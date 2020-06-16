
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
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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
    private WarehouseService warehouseService;

    @Autowired
    private TruckService truckService;

    private TPA newTpa;

    @Before
    public void init() {
        newTpa = new TPA();
        newTpa.setName("TPA_test");
    }

    /**
     * Check the attempt of updating the TPA. Status 200 should be returned.
     *
     * @throws Exception for mockMvc
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
        mockMvc.perform(put("/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "TPA with ID=27 was successfully updated"));

        mockMvc.perform(get("/tpa/27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New_Name"))
                .andExpect(jsonPath("$.departurePlan").value("2030-05-28T15:15"));

    }

    /**
     * Check the attempt of updating the TPA with Name and DeparturePlan in wrong format. Status 412 should be returned.
     *
     * @throws Exception for mockMvc
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
        mockMvc.perform(put("/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("TPA-name_Pattern", "must match \"^[0-9A-Za-z\\-_]+\""))
                .andExpect(header().stringValues("TPA-departurePlan_Pattern", "must match \"^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$\""));

        mockMvc.perform(get("/tpa/27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST_TPA"))
                .andExpect(jsonPath("$.departurePlan").value("2020-05-25T10:00"));
    }

    /**
     * Case when given TPA has id which doesn't exist in DB.
     * Status 404 should be returned
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTestOfTpaResponse404() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaID(10000L);
        String json = om.writeValueAsString(newTpa);

        mockMvc.perform(put("/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Message:", "Given TPA does not exist in DB and could not be updated"));
    }

    /**
     * Case when given TPA has null value for ID
     * Status 404 should be returned
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTestOfTpaResponse404NullGiven() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(newTpa);

        mockMvc.perform(put("/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("ERROR", "Not Existing"));
    }

    /**
     * Case when user tries to update TPA which has status CLOSED.
     * Status 422 should be returned
     *
     * @throws Exception for mockMvc
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
        mockMvc.perform(put("/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Message:", "Given Dates are in the Past or The TPA id=26 is CLOSED"));
    }

    /**
     * Case when user tries to update TPA by providing the Departure Date Plan which is in the Past
     * Status 422 should be returned
     *
     * @throws Exception for mockMvc
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
        mockMvc.perform(put("/tpa/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Message:", "Given Dates are in the Past or The TPA id=27 is CLOSED"));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity without provided
     * planned ETD time.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void createNewTpaWrongConstraints412() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("TPA-departurePlan_NotBlank", "must not be blank"))
                .andExpect(header().stringValues("TPA-departurePlan_NotNull", "must not be null"));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity with planned ETD time which is in the Past.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void createNewTpaEtdInThePast() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        newTpa.setDeparturePlan("2020-05-25T10:00");
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Error:", "The ETD of the TPA is in the Past."));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity with planned ETD time which is in the
     * very same day of creation. The status of TPA should be IN_PROGRESS
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void createNewTpaEtdHasActualDate() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        log.info(LocalDateTime.now().toString());
        newTpa.setDeparturePlan(LocalDateTime.now().plusHours(1L).toString().substring(0, 16));
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The TPA name=TPA_test was successfully saved in Warehouse XD Gro"))
                .andExpect(jsonPath("$.['status'].statusName").value("IN_PROGRESS"));
    }

    /**
     * Case when user tries to create TPA manually by passing TPA entity with planned ETD time which is in the
     * future and not at the same day of creation. The status of TPA should be BUFFER
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void createNewTpaEtdHasDateInFuture() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTpa.setTpaDaysSetting(truckService.getTpaDaysSettingsService().getTpaDaySettingsById(14L));
        log.info(LocalDateTime.now().toString());
        newTpa.setDeparturePlan(LocalDateTime.now().plusDays(1L).plusHours(1L).toString().substring(0, 16));
        String json = om.writeValueAsString(newTpa);
        mockMvc.perform(post("/tpa/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The TPA name=TPA_test was successfully saved in Warehouse XD Gro"))
                .andExpect(jsonPath("$.['status'].statusName").value("BUFFER"));
    }

    /**
     * Case when given url-code of the Warehouse wasn't found in Database
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest200() throws Exception {
        mockMvc.perform(get("/cc_arad/tpa/2020-05-06"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().is(200));
    }

    /**
     * Case when given url-code of the Warehouse wasn't found in Database
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest404() throws Exception {
        mockMvc.perform(get("/not_exist/tpa/2020-05-06"))
                .andDo(print())
                .andExpect(header().stringValues("Error:", "The Warehouse with url-code:\"not_exist\" wasn't found"))
                .andExpect(status().is(404));
    }

    /**
     * Case when given date has value which not corresponds to regex condition:
     * tpaDepartureDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest404BadDate() throws Exception {
        mockMvc.perform(get("/cc_arad/tpa/2020-25-06"))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * Case when date formatter in TpaController throws Exception.
     * It could happen when month value is in range [13-19]
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void getListOfTpaByWarehouseAndDayTest400BadDateWithException() throws Exception {
        mockMvc.perform(get("/cc_arad/tpa/2020-15-06"))
                .andDo(print())
                .andExpect(status().is(400));
    }

    /**
     * Attempt to delete TPA by not existing Id. Response status 404
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTpaByIdWhichNotExistsTestStatus404() throws Exception {
        mockMvc.perform(delete("/tpa/100"))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "TPA with id=100 wasn't found"));
    }

    /**
     * Attempt to delete TPA which has Manifests in ManifestSet. Response status 417
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTpaByIdWithManifestsInSetTestStatus417() throws Exception {
        mockMvc.perform(delete("/tpa/23"))
                .andDo(print())
                .andExpect(status().is(417))
                .andExpect(header().stringValues("Error:", "TPA with id=23 has not empty set of Manifests or References and couldn't be deleted"));
    }

    /**
     * Attempt to delete TPA which has References in Set. Response 417
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTpaByIdWithReferencesInSetTestStatus417() throws Exception {
        mockMvc.perform(delete("/tpa/25"))
                .andDo(print())
                .andExpect(status().is(417))
                .andExpect(header().stringValues("Error:", "TPA with id=25 has not empty set of Manifests or References and couldn't be deleted"));
    }

    /**
     * Attempt to delete CLOSED TPA. Response status 403
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTpaByIdWithStatusClosedTestStatus403() throws Exception {
        mockMvc.perform(delete("/tpa/26"))
                .andDo(print())
                .andExpect(status().is(403))
                .andExpect(header().stringValues("Error:", "TPA with id=26 has status CLOSED and couldn't be deleted"));

    }

    /**
     * Test of attempt to delete the Tpa
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTpaByIdStatus200() throws Exception {
        mockMvc.perform(delete("/tpa/1"))
                .andDo(print())
                .andExpect(status().is(204))
                .andExpect(header().stringValues("Message:", "TPA with id=1 was successfully deleted"));
    }
}
