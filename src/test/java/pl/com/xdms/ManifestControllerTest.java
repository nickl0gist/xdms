package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.ManifestService;
import pl.com.xdms.service.truck.TruckService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 03.06.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = {"/sql_scripts/createValuesInDBforTestsOfTttTpaManifestControllers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class ManifestControllerTest {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MockMvc mockMvc;

    private Manifest newManifest;

    @Autowired
    private ManifestService manifestService;

    @Autowired
    private ManifestReferenceService manifestReferenceService;

    @Autowired
    private TruckService truckService;

    @Before
    public void init() {
        newManifest = new Manifest();
        newManifest.setManifestCode("Manifest_Code");
    }

    /**
     * Test of searching for Abandoned Manifest, which are not relied to any existing TTT
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void getAllAbandonedManifestsTest() throws Exception {
        mockMvc.perform(get("/manifests/abandoned"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(status().isOk());
    }

    /**
     * Ok test for searching manifest by given id.
     *
     * @throws Exception mockMvc
     */
    @Test
    public void getManifestByIdTest200() throws Exception {
        mockMvc.perform(get("/manifest/10"))
                .andDo(print())
                .andExpect(jsonPath("$.manifestID").value(10))
                .andExpect((status().isOk()));
    }

    /**
     * NOK test for searching manifest by given id.
     *
     * @throws Exception mockMvc
     */
    @Test
    public void getManifestByIdTest404() throws Exception {
        mockMvc.perform(get("/manifest/250"))
                .andDo(print())
                .andExpect(header().stringValues("Error:", "The manifest with id=250 is not existing"))
                .andExpect((status().isNotFound()));
    }

    /**
     * Test of attempt to update manifest with Ok respond status
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateManifestTestResponse200() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        Manifest manifestToUpdate = entityManager.find(Manifest.class, 5L);// (5, 'TEST-MAN-03', 1, 3, 1.2, 1000.0, 3, 3),
        manifestToUpdate.getManifestsReferenceSet().iterator();
        manifestToUpdate.getTpaSet().iterator();
        manifestToUpdate.getTruckTimeTableSet().iterator();

        entityManager.detach(manifestToUpdate);

        manifestToUpdate.setTotalWeightReal(5000.0);
        manifestToUpdate.setTotalLdmReal(5.4);
        manifestToUpdate.setPalletQtyReal(10);
        manifestToUpdate.setBoxQtyReal(500);

        String json = om.writeValueAsString(manifestToUpdate);
        entityManager.getTransaction().commit();
        entityManager.close();

        Manifest manifestToCheck = manifestService.findManifestById(5L);
        Assert.assertNull(manifestToCheck.getBoxQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());
        Assert.assertNull(manifestToCheck.getPalletQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());

        mockMvc.perform(put("/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The Manifest with id=5 was successfully updated"));

        mockMvc.perform(get("/manifest/5"))
                .andDo(print())
                .andExpect(jsonPath("$.totalWeightReal").value(5000.0))
                .andExpect(jsonPath("$.totalLdmReal").value(5.4))
                .andExpect(jsonPath("$.palletQtyReal").value(10))
                .andExpect(jsonPath("$.boxQtyReal").value(500));
    }

    /**
     * Attempt to update the manifest which doesn't have id
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateManifestTestResponse400() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newManifest.setTotalWeightReal(5000.0);
        newManifest.setTotalLdmReal(5.4);
        newManifest.setPalletQtyReal(10);
        newManifest.setBoxQtyReal(500);

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(put("/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("ERROR", "Not Existing"));
    }

    /**
     * Test of case when user tries to delete the Manifest by it's given Id.
     * IN this testcase the manifest doesn't have any References related.
     * Assertions check the size of the sets of Manifests in related TPA and TTT after
     * deletion was performed.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteDirectManifestStatus200() throws Exception {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        Set<Manifest> tttManifestSetBefore = entityManager.find(TruckTimeTable.class, 14L).getManifestSet();
        Set<Manifest> tpaManifestSetBefore = entityManager.find(TPA.class, 14L).getManifestSet();

        Assert.assertEquals(1, tttManifestSetBefore.size());
        Assert.assertEquals(1, tpaManifestSetBefore.size());

        entityManager.getTransaction().commit();
        entityManager.close();

        mockMvc.perform(delete("/manifest/10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Message:", "Manifest with id=10 deleted"));

        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        entityManager2.getTransaction().begin();

        Set<Manifest> tttManifestSetAfter = entityManager2.find(TruckTimeTable.class, 14L).getManifestSet();
        Set<Manifest> tpaManifestSetAfter = entityManager2.find(TPA.class, 14L).getManifestSet();

        Assert.assertEquals(0, tttManifestSetAfter.size());
        Assert.assertEquals(0, tpaManifestSetAfter.size());

        entityManager2.getTransaction().commit();
        entityManager2.close();
    }

    /**
     * Test of the case when user tries to delete manifest with References inside.
     * According to architecture the manifest_References entities should be deleted by cascade
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestWithReferencesStatus200() throws Exception {

        Assert.assertNotNull(manifestReferenceService.findById(7L));

        mockMvc.perform(delete("/manifest/13"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Message:", "Manifest with id=13 deleted"));

        Assert.assertNull(manifestReferenceService.findById(7L));
    }

    /**
     * Testcase of attempt to delete manifest with real quantities provided
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestWithRealQuantitiesProvidedStatus422Test() throws Exception {
        mockMvc.perform(delete("/manifest/1"))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().stringValues("Message:", "Manifest with id=1 arrived already and couldn't be deleted"));
    }

    /**
     * Testcase when given id wasn't found while deleting the Manifest
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestWithWrongId404Test() throws Exception {
        mockMvc.perform(delete("/manifest/100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues("Message:", "Manifest with id=100 Not Found"));
    }
}
