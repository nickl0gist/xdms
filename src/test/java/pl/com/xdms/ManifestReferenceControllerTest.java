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
import org.springframework.test.web.servlet.MvcResult;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.service.ManifestReferenceService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 25.06.2020
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
public class ManifestReferenceControllerTest {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManifestReferenceService manifestReferenceService;

    private ManifestReference manifestReference;

    private ObjectMapper om;

    @Before
    public void init() {
        manifestReference = new ManifestReference();
        om = new ObjectMapper();
    }

    /**
     * Test of endpoint which used to obtain all ManifestReferences without assigned TPA.
     */
    @Test
    public void findAllAbandonedManifestReferencesTest() throws Exception {
        mockMvc.perform(get("/man_ref/abandoned"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].manifestReferenceId").value(12));
    }

    /**
     * Test case of certain ManifestReference Movement to another Tpa.
     */
    @Test
    public void moveManifestReferenceToAnotherTpaTest() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        ManifestReference manifestReferenceToUpdate = entityManager.find(ManifestReference.class, 11L);// (5, 'TEST-MAN-03', 1, 3, 1.2, 1000.0, 3, 3),

        entityManager.detach(manifestReferenceToUpdate);
        String json = om.writeValueAsString(manifestReferenceToUpdate);

        entityManager.getTransaction().commit();
        entityManager.close();

        Assert.assertEquals(25, (long) manifestReferenceToUpdate.getTpa().getTpaID());

        mockMvc.perform(put("/man_ref/move_to_tpa/24").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Message:", "The ManifestReference with ID=11 was moved to TPA ID=24"));

        Assert.assertEquals(24, (long) manifestReferenceService.findById(11L).getTpa().getTpaID());
    }

    /**
     * Testcase when user tries to move any ManifestReference entity to another Tpa.
     * The ManifestReference in this case doesn't have ID.
     */
    @Test
    public void moveManifestReferenceWithoutIdToAnotherTpaTest() throws Exception {
        ObjectMapper om = new ObjectMapper();

        manifestReference = manifestReferenceService.findById(11L);
        manifestReference.setManifestReferenceId(null);
        Assert.assertEquals(25, (long) manifestReference.getTpa().getTpaID());
        String json = om.writeValueAsString(manifestReference);

        mockMvc.perform(put("/man_ref/move_to_tpa/24").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Testcase when user tries to move any ManifestReference entity to another Tpa which not Existing in DB.
     */
    @Test
    public void moveManifestReferenceToNotExistingTpaTest() throws Exception {
        ObjectMapper om = new ObjectMapper();

        manifestReference = manifestReferenceService.findById(11L);
        Assert.assertEquals(25, (long) manifestReference.getTpa().getTpaID());
        String json = om.writeValueAsString(manifestReference);

        mockMvc.perform(put("/man_ref/move_to_tpa/200").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(417))
                .andExpect(header().string("Error:", "The TPA with ID=200 wasn't found"));
    }

    /**
     * Testcase when user tries to move ManifestReference entity with ID which not existing in DB
     * to another Tpa.
     */
    @Test
    public void moveNotExistingManifestReferenceToAnotherTpaTest() throws Exception {
        ObjectMapper om = new ObjectMapper();

        manifestReference.setManifestReferenceId(1000L);
        String json = om.writeValueAsString(manifestReference);

        mockMvc.perform(put("/man_ref/move_to_tpa/20").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(417))
                .andExpect(header().string("Error:", "The ManifestReference with ID=1000 wasn't found"));
    }

    /**
     * Test of attempt to save List of updated ManifestReferences in DB.
     * if all conditions are ok return status 200.
     */
    @Test
    public void receptionForListOfManifestEntitiesTest() throws Exception {
        List<ManifestReference> theList = new ArrayList<>();
        ManifestReference mr1 = manifestReferenceService.findById(2L);
        ManifestReference mr2 = manifestReferenceService.findById(3L);
        ManifestReference mr3 = manifestReferenceService.findById(4L);
        mr1.setDeliveryNumber("DN_TEST_01");
        mr1.setReceptionNumber("2555320001");
        mr2.setDeliveryNumber("DN_TEST_02");
        mr2.setReceptionNumber("2555320002");
        mr3.setDeliveryNumber("DN_TEST_03");
        mr3.setReceptionNumber("2555320003");

        theList.add(mr1);
        theList.add(mr2);
        theList.add(mr3);

        String json = om.writeValueAsString(theList);

        mockMvc.perform(put("/man_ref/reception").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.manifestReferenceId == 2 && @.receptionNumber == \"2555320001\" && @.deliveryNumber == \"DN_TEST_01\")]").exists())
                .andExpect(jsonPath("$[?(@.manifestReferenceId == 3 && @.receptionNumber == \"2555320002\" && @.deliveryNumber == \"DN_TEST_02\")]").exists())
                .andExpect(jsonPath("$[?(@.manifestReferenceId == 4 && @.receptionNumber == \"2555320003\" && @.deliveryNumber == \"DN_TEST_03\")]").exists());
    }

    /**
     * Attempt of updating reception information for each element of <code>List<ManifestReference></></code>.
     * TestCase when one element of the List has Id which not found im DB.
     */
    @Test
    public void receptionForListOfManifestEntitiesWithOneWrongEntityTest() throws Exception {
        List<ManifestReference> theList = new ArrayList<>();
        ManifestReference mr1 = manifestReferenceService.findById(2L);
        ManifestReference mr2 = manifestReferenceService.findById(3L);
        ManifestReference mr3 = manifestReferenceService.findById(4L);
        mr1.setDeliveryNumber("DN_TEST_01");
        mr1.setReceptionNumber("2555320001");
        mr2.setDeliveryNumber("DN_TEST_02");
        mr2.setReceptionNumber("2555320002");
        mr3.setDeliveryNumber("DN_TEST_03");
        mr3.setReceptionNumber("2555320003");
        mr2.setManifestReferenceId(200L);

        theList.add(mr1);
        theList.add(mr2);
        theList.add(mr3);

        String json = om.writeValueAsString(theList);

        mockMvc.perform(put("/man_ref/reception").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.manifestReferenceId == 2 && @.receptionNumber == \"2555320001\" && @.deliveryNumber == \"DN_TEST_01\")]").exists())
                .andExpect(jsonPath("$[?(@.manifestReferenceId == 3 && @.receptionNumber == \"2555320002\" && @.deliveryNumber == \"DN_TEST_02\")]").doesNotExist())
                .andExpect(jsonPath("$[?(@.manifestReferenceId == 4 && @.receptionNumber == \"2555320003\" && @.deliveryNumber == \"DN_TEST_03\")]").exists());
    }

    /**
     * Attempt to receipt ManifestReferences with Annotation condition violations in the Class ManifestReference
     */
    @Test
    public void receptionManifestReferenceWithAnnotationViolationsTest() throws Exception {
        List<ManifestReference> theList = new ArrayList<>();
        ManifestReference mr1 = manifestReferenceService.findById(2L);
        ManifestReference mr2 = manifestReferenceService.findById(3L);
        ManifestReference mr3 = manifestReferenceService.findById(4L);
        mr1.setDeliveryNumber("DN/TEST/01");
        mr1.setReceptionNumber("2555320001");
        mr1.setQtyReal(-5);
        mr2.setDeliveryNumber("DN_TEST_02");
        mr2.setReceptionNumber("2555320002");
        mr2.setGrossWeightReal(-100);
        mr3.setDeliveryNumber("DN_TEST_03");
        mr3.setReceptionNumber("2555320003");
        mr3.setReference(null);

        theList.add(mr1);
        theList.add(mr2);
        theList.add(mr3);

        String json = om.writeValueAsString(theList);

        MvcResult result = mockMvc.perform(put("/man_ref/reception").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isBadRequest()).andReturn();

        String headerError = result.getResponse().getHeader("Error:");
        Assert.assertNotNull(headerError);
        Assert.assertTrue(headerError.contains("reception.manifestReferenceList[2].reference: must not be null"));
        Assert.assertTrue(headerError.contains("reception.manifestReferenceList[0].deliveryNumber: must match \"^[0-9A-Za-z\\-_]+\""));
        Assert.assertTrue(headerError.contains("reception.manifestReferenceList[0].qtyReal: must be greater than or equal to 0"));
        Assert.assertTrue(headerError.contains("reception.manifestReferenceList[1].grossWeightReal: must be greater than or equal to 0"));
    }

}
