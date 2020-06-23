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
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.ManifestService;
import pl.com.xdms.service.SupplierService;
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
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

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
        mockMvc.perform(get("/ttt/manifests/abandoned"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
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

        Assert.assertEquals(TTTEnum.PENDING, truckService.getTttService().getTttById(5L).getTttStatus().getTttStatusName());
        Assert.assertNull(truckService.getTttService().getTttById(5L).getTttArrivalDateReal());

        mockMvc.perform(put("/ttt/5/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The Manifest with id=5 was successfully updated"));

        mockMvc.perform(get("/manifest/5"))
                .andDo(print())
                .andExpect(jsonPath("$.totalWeightReal").value(5000.0))
                .andExpect(jsonPath("$.totalLdmReal").value(5.4))
                .andExpect(jsonPath("$.palletQtyReal").value(10))
                .andExpect(jsonPath("$.boxQtyReal").value(500));

        Assert.assertEquals(TTTEnum.ARRIVED, truckService.getTttService().getTttById(5L).getTttStatus().getTttStatusName());
        Assert.assertNotNull(truckService.getTttService().getTttById(5L).getTttArrivalDateReal());
    }

    /**
     * Testcase of update manifest request within TTT which doesn't have the Manifest in it's set.
     *
     * @throws Exception for mockMvc.
     */
    @Test
    public void updateManifestTestWithinTttWhichDoesntHaveThisManifestResponse200() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        Manifest manifestToUpdate = entityManager.find(Manifest.class, 10L);
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

        Manifest manifestToCheck = manifestService.findManifestById(10L);
        Assert.assertNull(manifestToCheck.getBoxQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());
        Assert.assertNull(manifestToCheck.getPalletQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());

        mockMvc.perform(put("/ttt/5/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().string("ERROR", "Not Existing"));
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

        mockMvc.perform(put("/ttt/5/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400));
    }

    /**
     * Testcase of request when there is an attempt to update manifest in not existing TTT
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateManifestWithinNotExistingTttTestResponse400() throws Exception {
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

        mockMvc.perform(put("/ttt/1000/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().string("ERROR", "Not Existing"));
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

    /**
     * Testcase of attempt to add new manifest in chosen TTT.
     * The provided manifest meets to conditions and snd it would be saved in DB.
     * Status response - 200 (Ok).
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void manualCreationOfManifestTestStatus200() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();

        newManifest.setManifestCode("NEW-MANIFEST-11");
        newManifest.setTotalWeightPlanned(500.0);
        newManifest.setTotalLdmPlanned(3.0);
        newManifest.setPalletQtyPlanned(10);
        newManifest.setBoxQtyPlanned(100);
        newManifest.setCustomer(customerService.getCustomerById(1L));
        newManifest.setSupplier(supplierService.getSupplierById(1L));

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The manifest NEW-MANIFEST-11 was successfully saved with id=20"));
    }

    /**
     * Testcase of attempt to add new manifest in chosen TTT.
     * The provided manifest has inactive Customer and Supplier
     * and it wouldn't be persisted in DB.
     * Response - 409 (Conflict).
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void manualCreationOfManifestTestStatus409SupplierCustomerNotActive() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();

        newManifest.setManifestCode("NEW-MANIFEST-11");
        newManifest.setTotalWeightPlanned(500.0);
        newManifest.setTotalLdmPlanned(3.0);
        newManifest.setPalletQtyPlanned(10);
        newManifest.setBoxQtyPlanned(100);
        newManifest.setCustomer(customerService.getCustomerById(4L));
        newManifest.setSupplier(supplierService.getSupplierById(4L));

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(409))
                .andExpect(header().stringValues("Error:", "Manifest with code=NEW-MANIFEST-11 has Given Supplier isActive = false, Customer isActive = false"));
    }

    /**
     * Testcase of attempt to save the Manifest with break of Manifest.class annotation conditions.
     * Response - 412 (Precondition Failed).
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void manualCreationOfManifestTestStatus412() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();
        newManifest.setBoxQtyPlanned(-10);

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("manifest-supplier_NotNull", "must not be null"))
                .andExpect(header().stringValues("manifest-customer_NotNull", "must not be null"))
                .andExpect(header().stringValues("manifest-boxQtyPlanned_Min", "must be greater than or equal to 0"));
    }

    /**
     * Test of attempt to add manifest within not existing TTT.
     * Response - 404 (Not found).
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void manualCreationOfManifestTestStatus404() throws Exception {
        long tttId = 280L;
        ObjectMapper om = new ObjectMapper();
        newManifest.setBoxQtyPlanned(-10);

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "The TTT with Id=280 wasn't found"));
    }

    /**
     * Testcase of attempt to save Manifest with ManifestCode which already exists in DB
     * Response - - 409 (Conflict).
     *
     * @throws Exception fro mockMvc.
     */
    @Test
    public void manualCreationOfManifestTestStatus409() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();
        newManifest.setManifestCode("TEST-MAN-01"); // id=3 in DB
        newManifest.setTotalWeightPlanned(500.0);
        newManifest.setTotalLdmPlanned(3.0);
        newManifest.setPalletQtyPlanned(10);
        newManifest.setBoxQtyPlanned(100);
        newManifest.setCustomer(customerService.getCustomerById(1L));
        newManifest.setSupplier(supplierService.getSupplierById(1L));

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(409))
                .andExpect(header().stringValues("Error:", "Manifest with code=TEST-MAN-01 is existing in DB already"));
    }

    /**
     * Test of attempt to delete Manifest in TPA which does not exist.
     *
     * @throws Exception for mockNvc
     */
    @Test
    public void deleteManifestFromGivenTpaTest404() throws Exception {
        mockMvc.perform(delete("/tpa/200/manifest/3"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues("Error:", "TPA with id=200 wasn't found"));
    }

    /**
     * Attempt of removing particular Manifest from Set of given TPA by id.
     * Response 200.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestFromGivenTpaTest200() throws Exception {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TPA tpa = entityManager.find(TPA.class, 4L);
        Assert.assertEquals(2, tpa.getManifestSet().size());
        entityManager.getTransaction().commit();

        mockMvc.perform(delete("/tpa/4/manifest/6"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "Manifest TEST-MAN-04 was removed from TPA with id=4"))
                .andExpect(jsonPath("$.tpaID").value(4))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(1)));

    }

    /**
     * Test of case when user tries to delete unknown manifest from existing TPA.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestFromGivenTpaTest405() throws Exception {
        mockMvc.perform(delete("/tpa/2/manifest/300"))
                .andDo(print())
                .andExpect(status().is(405))
                .andExpect(header().stringValues("Error:", "Manifest with id=300 wasn't found in DB"))
                .andExpect(jsonPath("$.tpaID").value(2));
    }

    /**
     * Test of attempt to delete Manifest in TTT which does not exist.
     *
     * @throws Exception for mockNvc
     */
    @Test
    public void deleteManifestFromGivenTttTest404() throws Exception {
        mockMvc.perform(delete("/ttt/200/manifest/3"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues("Error:", "TTT with id=200 wasn't found"));
    }

    /**
     * Test of case when user tries to delete unknown manifest from existing TTT.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestFromGivenTttTest405() throws Exception {
        mockMvc.perform(delete("/ttt/2/manifest/300"))
                .andDo(print())
                .andExpect(status().is(405))
                .andExpect(header().stringValues("Error:", "Manifest with id=300 wasn't found in DB"))
                .andExpect(jsonPath("$.tttID").value(2));
    }

    /**
     * Attempt of removing particular Manifest from Set of given TTT by id.
     * Response 200.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteManifestFromGivenTttTest200() throws Exception {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TruckTimeTable ttt = entityManager.find(TruckTimeTable.class, 16L);
        Assert.assertEquals(2, ttt.getManifestSet().size());
        entityManager.getTransaction().commit();

        mockMvc.perform(delete("/ttt/16/manifest/7"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "Manifest MAN-X-01 was removed from TTT with id=16"))
                .andExpect(jsonPath("$.tttID").value(16))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(1)));

    }

    /**
     * TestCase of attempt to delete manifest from Closed TPA.
     *
     * @throws Exception mockMvc
     */
    @Test
    public void deleteManifestFromGivenTpaTest400() throws Exception {
        TPA tpa = truckService.getTpaService().getTpaById(4L);
        tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.CLOSED));

        log.info("TPA {}", truckService.getTpaService().save(tpa).getStatus());

        mockMvc.perform(delete("/tpa/4/manifest/6"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("Error:", "TPA with id=4 has bean already CLOSED"))
                .andExpect(jsonPath("$.tpaID").value(4))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }
}
