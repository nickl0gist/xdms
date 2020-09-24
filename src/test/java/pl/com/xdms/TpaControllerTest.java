
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
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.WarehouseManifestService;
import pl.com.xdms.service.truck.TruckService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 12.04.2020
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
public class TpaControllerTest {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManifestReferenceService manifestReferenceService;

    @Autowired
    private TruckService truckService;

    @Autowired
    private WarehouseManifestService warehouseManifestService;
    private TPA newTpa;

    @Before
    public void init() {
        newTpa = new TPA();
        newTpa.setName("TPA_test");
    }

    /**
     * Check the attempt of updating the TPA. Status 200 should be returned.
     */
    @Test
    public void updateTestOfTpaResponse200() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        TPA tpaToUpdate = entityManager.find(TPA.class, 27L);// (27, 'TEST_TPA',   3, '2020-05-25T10:00', 51)
        Set<ManifestReference> manifestReferenceSet = tpaToUpdate.getManifestReferenceSet();
        Set<Manifest> manifestSet = tpaToUpdate.getManifestSet();

        //Iterating through the Sets of Manifests and ManifestReferences to call these entities and avoid LazyLoadException
        manifestReferenceSet.iterator();
        manifestSet.iterator();
        //Detaching of the Entity from transaction
        entityManager.detach(tpaToUpdate);

        tpaToUpdate.setName("New_Name");
        tpaToUpdate.setDeparturePlan("2030-05-28T15:15");

        String json = om.writeValueAsString(tpaToUpdate);
        entityManager.getTransaction().commit();
        entityManager.close();

        Assert.assertEquals("TEST_TPA", truckService.getTpaService().getTpaById(27L).getName());
        mockMvc.perform(put("/warehouse/xd_gro/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "TPA with ID=27 was successfully updated"));

        mockMvc.perform(get("/warehouse/xd_gro/tpa/27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New_Name"))
                .andExpect(jsonPath("$.departurePlan").value("2030-05-28T15:15"));

    }

    /**
     * Check the attempt of updating the TPA with Name and DeparturePlan in wrong format. Status 412 should be returned.
     */
    @Test
    public void updateTestOfTpaResponse412() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        TPA tpaToUpdate = entityManager.find(TPA.class, 27L);//truckService.getTpaService().getTpaById(24L);
        Set<ManifestReference> manifestReferenceSet = tpaToUpdate.getManifestReferenceSet();
        Set<Manifest> manifestSet = tpaToUpdate.getManifestSet();

        //Iterating through the Sets of Manifests and ManifestReferences to call these entities and avoid LazyLoadException
        manifestReferenceSet.iterator();
        manifestSet.iterator();
        //Detaching of the Entity from transaction
        entityManager.detach(tpaToUpdate);

        tpaToUpdate.setName("!New-TPA!!");
        tpaToUpdate.setDeparturePlan("2020-0b-28T15:15");

        String json = om.writeValueAsString(tpaToUpdate);
        entityManager.getTransaction().commit();
        entityManager.close();

        Assert.assertEquals("TEST_TPA", truckService.getTpaService().getTpaById(27L).getName());
        mockMvc.perform(put("/warehouse/xd_gro/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("TPA-name_Pattern", "must match \"^[0-9A-Za-z\\-_]+\""))
                .andExpect(header().stringValues("TPA-departurePlan_Pattern", "must match \"^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$\""));

        mockMvc.perform(get("/warehouse/xd_gro/tpa/27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST_TPA"))
                .andExpect(jsonPath("$.departurePlan").value("2020-05-25T10:00"));
    }

    /**
     * Case when given TPA has id which doesn't exist in DB.
     * Status 404 should be returned
     */
    @Test
    public void updateTestOfTpaResponse404() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaID(10000L);
        String json = om.writeValueAsString(newTpa);

        mockMvc.perform(put("/warehouse/xd_gro/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Message:", "Given TPA does not exist in DB and could not be updated"));
    }

    /**
     * Case when given TPA has null value for ID
     * Status 404 should be returned
     */
    @Test
    public void updateTestOfTpaResponse404NullGiven() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(newTpa);

        mockMvc.perform(put("/warehouse/xd_gro/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("ERROR", "Not Existing"));
    }

    /**
     * Case when user tries to update TPA which has status CLOSED.
     * Status 422 should be returned
     */
    @Test
    public void updateTpaResponse422ClosedTest() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        TPA tpaToUpdate = entityManager.find(TPA.class, 26L);//(26, 'CLOSED_TPA',  1, '2020-05-20T20:00', 35),
        Set<ManifestReference> manifestReferenceSet = tpaToUpdate.getManifestReferenceSet();
        Set<Manifest> manifestSet = tpaToUpdate.getManifestSet();

        //Iterating through the Sets of Manifests and ManifestReferences to call these entities and avoid LazyLoadException
        manifestReferenceSet.iterator();
        manifestSet.iterator();
        //Detaching of the Entity from transaction
        entityManager.detach(tpaToUpdate);

        tpaToUpdate.setName("New_Name");
        tpaToUpdate.setDeparturePlan("2020-05-28T15:15");

        String json = om.writeValueAsString(tpaToUpdate);
        entityManager.getTransaction().commit();
        entityManager.close();

        Assert.assertEquals("CLOSED_TPA", truckService.getTpaService().getTpaById(26L).getName());
        mockMvc.perform(put("/warehouse/xd_gro/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Message:", "Given Dates are in the Past or The TPA id=26 is CLOSED"));
    }

    /**
     * Case when user tries to update TPA by providing the Departure Date Plan which is in the Past
     * Status 422 should be returned
     */
    @Test
    public void updateTpaResponseDateInThePast422Test() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        TPA tpaToUpdate = entityManager.find(TPA.class, 27L);//(27, 'TEST_TPA',   3, '2020-05-25T10:00', 51);
        Set<ManifestReference> manifestReferenceSet = tpaToUpdate.getManifestReferenceSet();
        Set<Manifest> manifestSet = tpaToUpdate.getManifestSet();

        //Iterating through the Sets of Manifests and ManifestReferences to call these entities and avoid LazyLoadException
        manifestReferenceSet.iterator();
        manifestSet.iterator();
        //Detaching of the Entity from transaction
        entityManager.detach(tpaToUpdate);

        tpaToUpdate.setName("New_Name");
        tpaToUpdate.setDeparturePlan("2020-05-05T15:15");

        String json = om.writeValueAsString(tpaToUpdate);
        entityManager.getTransaction().commit();
        entityManager.close();

        Assert.assertEquals("TEST_TPA", truckService.getTpaService().getTpaById(27L).getName());
        mockMvc.perform(put("/warehouse/xd_gro/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Message:", "Given Dates are in the Past or The TPA id=27 is CLOSED"));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity without provided
     * planned ETD time.
     */
    @Test
    public void createNewTpaWrongConstraints412() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/warehouse/xd_gro/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("TPA-departurePlan_NotBlank", "must not be blank"))
                .andExpect(header().stringValues("TPA-departurePlan_NotNull", "must not be null"));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity with planned ETD time which is in the Past.
     */
    @Test
    public void createNewTpaEtdInThePast() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        newTpa.setDeparturePlan("2020-05-25T10:00");
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/warehouse/xd_gro/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Error:", "The ETD of the TPA is in the Past."));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity with planned ETD time which is in the
     * very same day of creation. The status of TPA should be IN_PROGRESS
     */
    @Test
    public void createNewTpaEtdHasActualDate() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        log.info(LocalDateTime.now().toString());
        newTpa.setDeparturePlan(LocalDateTime.now().plusHours(1L).toString().substring(0, 16));
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/warehouse/xd_gro/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The TPA name=TPA_test was successfully saved in Warehouse XD Gro"))
                .andExpect(jsonPath("$.['status'].statusName").value("IN_PROGRESS"));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity with planned ETD time which is in the
     * future and not at the same day of creation. The status of TPA should be BUFFER
     */
    @Test
    public void createNewTpaEtdHasDateInFuture() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        log.info(LocalDateTime.now().toString());
        newTpa.setDeparturePlan(LocalDateTime.now().plusDays(1L).plusHours(1L).toString().substring(0, 16));
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/warehouse/xd_gro/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The TPA name=TPA_test was successfully saved in Warehouse XD Gro"))
                .andExpect(jsonPath("$.['status'].statusName").value("BUFFER"));
    }

    /**
     * Case when given url-code of the Warehouse wasn't found in Database
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest200() throws Exception {
        mockMvc.perform(get("/warehouse/cc_arad/tpa/2020-05-06"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().is(200));
    }

    /**
     * Case when given url-code of the Warehouse wasn't found in Database
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest404() throws Exception {
        mockMvc.perform(get("/warehouse/not_ex/tpa/2020-05-06"))
                .andDo(print())
                .andExpect(header().stringValues("Error:", "The Warehouse with url-code:\"not_ex\" wasn't found"))
                .andExpect(status().is(404));
    }

    /**
     * Case when given date has value which not corresponds to regex condition:
     * tpaDepartureDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest404BadDate() throws Exception {
        mockMvc.perform(get("/warehouse/cc_arad/tpa/2020-25-06"))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * Case when date formatter in TpaController throws Exception.
     * It could happen when month value is in range [13-19]
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest400BadDateWithException() throws Exception {
        mockMvc.perform(get("/warehouse/cc_arad/tpa/2020-15-06"))
                .andDo(print())
                .andExpect(status().is(400));
    }

    /**
     * Attempt to delete TPA by not existing Id. Response status 404
     */
    @Test
    public void deleteTpaByIdWhichNotExistsTestStatus404() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_arad/tpa/100"))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "TPA with id=100 wasn't found in scope of Warehouse cc_arad"));
    }

    /**
     * Attempt to delete TPA which has Manifests in ManifestSet. Response status 417
     */
    @Test
    public void deleteTpaByIdWithManifestsInSetTestStatus417() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_std/tpa/23"))
                .andDo(print())
                .andExpect(status().is(417))
                .andExpect(header().stringValues("Error:", "TPA with id=23 has not empty set of Manifests or References and couldn't be deleted"));
    }

    /**
     * Attempt to delete TPA which has References in Set. Response 417
     */
    @Test
    public void deleteTpaByIdWithReferencesInSetTestStatus417() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_gro/tpa/25"))
                .andDo(print())
                .andExpect(status().is(417))
                .andExpect(header().stringValues("Error:", "TPA with id=25 has not empty set of Manifests or References and couldn't be deleted"));
    }

    /**
     * Attempt to delete CLOSED TPA. Response status 403
     */
    @Test
    public void deleteTpaByIdWithStatusClosedTestStatus403() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_gro/tpa/26"))
                .andDo(print())
                .andExpect(status().is(403))
                .andExpect(header().stringValues("Error:", "TPA with id=26 has status CLOSED and couldn't be deleted"));

    }

    /**
     * Test of attempt to delete the Tpa
     */
    @Test
    public void deleteTpaByIdStatus200() throws Exception {
        //1. Check TPA which is assigned to manifest from TTT

        mockMvc.perform(delete("/warehouse/cc_swie/tpa/1"))
                .andDo(print())
                .andExpect(status().is(204))
                .andExpect(header().stringValues("Message:", "TPA with id=1 was successfully deleted"));
    }

    /**
     * Test of case when user tries to obtain all Delayed TPAs for particular Warehouse
     */
    @Test
    public void getAllDelayedTpaForWarehouse() throws Exception {
        mockMvc.perform(get("/warehouse/xd_gro/tpa/delayed"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.tpaID == 31 && @.['status'].statusName == \"DELAYED\")]").exists())
                .andExpect(jsonPath("$[?(@.tpaID == 33 && @.['status'].statusName == \"DELAYED\")]").exists());
    }

    /**
     * Test of case when user tries to obtain all In_Progress TPAs for particular Warehouse
     */
    @Test
    public void getAllInProgressTpaForWarehouse() throws Exception {
        mockMvc.perform(get("/warehouse/xd_gro/tpa/in_progress"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[?(@.tpaID == 32 && @.['status'].statusName == \"IN_PROGRESS\")]").exists())
                .andExpect(jsonPath("$[?(@.tpaID == 34 && @.['status'].statusName == \"IN_PROGRESS\")]").exists())
                .andExpect(jsonPath("$[?(@.tpaID == 35 && @.['status'].statusName == \"IN_PROGRESS\")]").exists());
    }

    /**
     * Test of case when user tries to obtain all Closed TPAs for particular Warehouse
     */
    @Test
    public void getAllClosedTpaForWarehouse() throws Exception {
        mockMvc.perform(get("/warehouse/xd_gro/tpa/closed"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.tpaID == 28 && @.['status'].statusName == \"CLOSED\")]").exists())
                .andExpect(jsonPath("$[?(@.tpaID == 29 && @.['status'].statusName == \"CLOSED\")]").exists());
    }

    /**
     * Test of case when user tries to obtain all BUFFER TPAs for particular Warehouse
     */
    @Test
    public void getAllBufferTpaForWarehouse() throws Exception {
        mockMvc.perform(get("/warehouse/xd_gro/tpa/buffer"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.tpaID == 36 && @.['status'].statusName == \"BUFFER\")]").exists())
                .andExpect(jsonPath("$[?(@.tpaID == 37 && @.['status'].statusName == \"BUFFER\")]").exists())
                .andExpect(jsonPath("$[?(@.tpaID == 38 && @.['status'].statusName == \"BUFFER\")]").exists());
    }

    /**
     * Test of case when user tries to obtain all NOT CLOSED TPAs for particular Warehouse
     */
    @Test
    public void getAllNotClosedTpaForWarehouse() throws Exception {
        mockMvc.perform(get("/warehouse/xd_gro/tpa/notClosed"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(16)));
    }

    /**
     * The test for case of attempt to split manifestReference.
     */
    @Test
    public void splitManifestReferenceTestOk() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReference = new ManifestReference();

        ManifestReference manifestReferenceSource = manifestReferenceService.findById(11L);
        manifestReferenceSource.setQtyReal(2500);
        manifestReferenceSource.setPalletQtyReal(4);
        manifestReferenceSource.setBoxQtyReal(150);
        manifestReferenceSource.setGrossWeightReal(1500);
        manifestReferenceSource.setNetWeight(1400);
        manifestReferenceService.save(manifestReferenceSource);

        manifestReference.setQtyReal(1000);
        manifestReference.setPalletQtyReal(2);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TPA tpaSource = entityManager.find(TPA.class, 25L);
        TPA tpaPlace = entityManager.find(TPA.class, 24L);

        tpaSource.getManifestReferenceSet().iterator();
        tpaPlace.getManifestReferenceSet().iterator();

        //Check set of Both involved TPAs before controller performed
        Assert.assertEquals(2, tpaSource.getManifestReferenceSet().size());
        Assert.assertEquals(2, tpaPlace.getManifestReferenceSet().size());
        entityManager.getTransaction().commit();
        entityManager.close();

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/11/tpa_to/24")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.tpaID").value(25))
                .andExpect(jsonPath("$.['manifestReferenceSet']", hasSize(2)))
                .andExpect(jsonPath("$.['manifestReferenceSet'][?(@.manifestReferenceId == 11 " +
                        "&& @.qtyReal == 1500.0 " +
                        "&& @.palletQtyReal == 2 " +
                        "&& @.grossWeightReal == 900.0 " +
                        "&& @.netWeight == 840.0 " +
                        "&& @.boxQtyReal == 80)]").exists())
                .andExpect(header().string("Message:", "ManifestReference with id=13 was successfully placed in TPA id=24"));

        //After the changes done, check if information in DB was changed
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        entityManager2.getTransaction().begin();
        TPA tpaSource2 = entityManager2.find(TPA.class, 25L);
        TPA tpaPlace2 = entityManager2.find(TPA.class, 24L);
        ManifestReference newManifestReference = entityManager2.find(ManifestReference.class, 13L);
        tpaSource2.getManifestReferenceSet().iterator();
        tpaPlace2.getManifestReferenceSet().iterator();

        Assert.assertEquals(2, tpaSource2.getManifestReferenceSet().size());
        Assert.assertEquals(3, tpaPlace2.getManifestReferenceSet().size());

        entityManager2.getTransaction().commit();
        entityManager2.close();

        log.info(newManifestReference.toString());
    }

    /**
     * The test for case of attempt to split manifestReference with not existing manifestReference id.
     */
    @Test
    public void splitManifestReferenceTestNokManifestReferenceNotFound() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setQtyReal(1000);
        manifestReference.setPalletQtyReal(2);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/1111/tpa_to/24")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().string("Error:", "The manifest which has to be split with id=1111 wasn't found"));
    }

    /**
     * The test for case of attempt to split manifestReference within TPA with id which does not exist in DB.
     */
    @Test
    public void splitManifestReferenceTestNokTpaNotFound() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setQtyReal(1000);
        manifestReference.setPalletQtyReal(2);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/11/tpa_to/2400")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().string("Error:", "The TPA with id=2400 where split part should be assigned does not exist. Or the TPA with id=25 where part has to be taken from doesn't exist in scope of Warehouse xd_gro"));
    }

    /**
     * The test for case of attempt to split manifestReference by providing greater quantities than in original ManifestReference
     */
    @Test
    public void splitManifestReferenceTestGreaterQty() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReferenceSource = manifestReferenceService.findById(11L);
        manifestReferenceSource.setQtyReal(2500);
        manifestReferenceSource.setPalletQtyReal(4);
        manifestReferenceSource.setBoxQtyReal(150);
        manifestReferenceSource.setGrossWeightReal(1500);
        manifestReferenceSource.setNetWeight(1400);
        manifestReferenceService.save(manifestReferenceSource);

        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setQtyReal(10000);
        manifestReference.setPalletQtyReal(2);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/11/tpa_to/24")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().string("Error:", "Qty of pcs, pallets or boxes of split manifestReference cannot be greater than origin one!"));
    }

    /**
     * The test for case of attempt to split manifestReference by providing same quantities as in original ManifestReference
     */
    @Test
    public void splitManifestReferenceTestTheSameQty() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReferenceSource = manifestReferenceService.findById(11L);
        manifestReferenceSource.setQtyReal(2500);
        manifestReferenceSource.setPalletQtyReal(4);
        manifestReferenceSource.setBoxQtyReal(150);
        manifestReferenceSource.setGrossWeightReal(1500);
        manifestReferenceSource.setNetWeight(1400);
        manifestReferenceService.save(manifestReferenceSource);

        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setQtyReal(1000);
        manifestReference.setPalletQtyReal(4);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/11/tpa_to/24")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().string("Error:", "Qty of pcs, pallets or boxes of split manifestReference cannot be greater than origin one!"));
    }

    /**
     * The test for case of attempt to split manifestReference which already placed in CLOSED TPA.
     */
    @Test
    public void splitManifestReferenceTestOriginTpaClosed() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReferenceSource = manifestReferenceService.findById(11L);
        manifestReferenceSource.setQtyReal(2500);
        manifestReferenceSource.setPalletQtyReal(4);
        manifestReferenceSource.setBoxQtyReal(150);
        manifestReferenceSource.setGrossWeightReal(1500);
        manifestReferenceSource.setNetWeight(1400);
        TPA tpa = manifestReferenceService.save(manifestReferenceSource).getTpa();
        tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.CLOSED));
        truckService.getTpaService().save(tpa);


        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setQtyReal(1000);
        manifestReference.setPalletQtyReal(4);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/11/tpa_to/24")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(403))
                .andExpect(header().string("Error:", "The TPA with id=25 where split part should be taken from is CLOSED"));
    }

    /**
     * The test for case of attempt to split manifestReference which already placed in CLOSED TPA.
     */
    @Test
    public void splitManifestReferenceTpaToPlaceClosed() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReferenceSource = manifestReferenceService.findById(11L);
        manifestReferenceSource.setQtyReal(2500);
        manifestReferenceSource.setPalletQtyReal(4);
        manifestReferenceSource.setBoxQtyReal(150);
        manifestReferenceSource.setGrossWeightReal(1500);
        manifestReferenceSource.setNetWeight(1400);

        TPA tpa = truckService.getTpaService().getTpaById(24L);
        tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.CLOSED));
        truckService.getTpaService().save(tpa);


        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setQtyReal(1000);
        manifestReference.setPalletQtyReal(4);
        manifestReference.setBoxQtyReal(70);

        String json = om.writeValueAsString(manifestReference);

        //Perform the controller call with ManifestReference 'manifestReference' attached
        mockMvc.perform((put("/warehouse/xd_gro/tpa/25/split/man_ref/11/tpa_to/24")).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(403))
                .andExpect(header().string("Error:", "The TPA with id=24 where split part should be placed to is CLOSED"));
    }

    /**
     * Test of creation .xlsx file with information about ManifestReferences in TTT for making reception.
     */
    @Test
    public void getPackingListTest() throws Exception{
        MvcResult result = mockMvc.perform(get("/warehouse/xd_gro/tpa/24/tpaPackingList.xlsx").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        File tempFile = File.createTempFile("test", ".xlsx", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(result.getResponse().getContentAsByteArray());
        log.info("Result {}", tempFile.getAbsolutePath());

//        //Map of the ManifestReference from received file after get request.
//        Map<Long, ManifestReference> testFileMap = excelManifestReferenceService.readExcel(tempFile);
//        //List of the ManifestReference from received testFileMap.
//        List<ManifestReference> manifestReferenceList = new ArrayList<>(testFileMap.values());
//        //List of ManifestReference created with key set from testFileMap
//        List<ManifestReference> referenceList = manifestReferenceService.getManRefListWithinIdSet(testFileMap.keySet());
//        Comparator<ManifestReference> comparator = Comparator.comparing(ManifestReference::getManifestReferenceId);
//        referenceList.sort(comparator);
//        manifestReferenceList.sort(comparator);
//        Assert.assertEquals(referenceList.stream().map(ManifestReference::getManifestReferenceId).collect(Collectors.toList()),
//        manifestReferenceList.stream().map(ManifestReference::getManifestReferenceId).collect(Collectors.toList()));
    }
}