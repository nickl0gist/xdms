package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
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
public class TruckTimeTableControllerTest {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private TruckService truckService;

    private TruckTimeTable newTtt;

    @Before
    public void init() {
        newTtt = new TruckTimeTable();
        newTtt.setTruckName("TPA_test");
    }

    @Test
    public void getAllTttByTheWarehouseAndCertainDate() throws Exception {
        mockMvc.perform(get("/cc_swie/ttt/2020-05-20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void getAllTttByTheWarehouseAndCertainDateWrongRegex() throws Exception {
        mockMvc.perform(get("/cc_swie/ttt/2020-04-233"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTttByIdTest() throws Exception {
        mockMvc.perform(get("/ttt/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckName").value("TPA1"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }

    @Test
    public void getTttByIdNotFound() throws Exception {
        mockMvc.perform(get("/ttt/101"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void createNewTtt() throws Exception {
        newTtt.setWarehouse(warehouseService.getWarehouseById(1L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.truckName").value("TPA_test"))
                .andExpect(jsonPath("$.['tttStatus'].tttStatusName").value("PENDING"));
        ;
    }

    @Test
    public void createNewTttStatusDelayed() throws Exception {
        newTtt.setWarehouse(warehouseService.getWarehouseById(1L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().minusDays(5).withSecond(0).withNano(0).toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.truckName").value("TPA_test"))
                .andExpect(jsonPath("$.['tttStatus'].tttStatusName").value("DELAYED"));
    }

    @Test
    public void createNewTttWithNotExistingWarehouse() throws Exception {

        newTtt.setWarehouse(warehouseService.getWarehouseById(100L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createNewTttWithNullWarehouse() throws Exception {
        newTtt.setWarehouse(null);
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Tests if the SQL requests in  createValuesInDBforTestsOfTttTpaManifestControllers.sql are OK
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void checkCreatedBySQLTttTest() throws Exception {

        TTT_FOR_CC:
        {
            mockMvc.perform(get("/cc_swie/ttt/2020-04-21"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/cc_irun/ttt/2020-04-21"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/cc_arad/ttt/2020-04-22"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        TTT_FOR_XD:
        {
            mockMvc.perform(get("/xd_std/ttt/2020-04-23"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/xd_std/ttt/2020-04-27"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

        }

        TTT_FOR_TXD:
        {
            mockMvc.perform(get("/xd_gro/ttt/2020-04-28"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    /**
     * Test set for checking if removing of the particular TTT for CC Warehouse works properly.
     * When user removes TTT in any CC all manifest from this TTT should be moved to new TTT in the XD with arrival date
     * the same as date in the TTT which used to deliver each manifest from CC to XD. The old TTT in XD will not contain any
     * manifests from removed TTT in CC.
     *
     * @throws Exception exception for mockMvc
     */
    @Test
    public void deleteTttFromCcTestWithChangingTttSetInXD() throws Exception {
        //1.Check how many TTTs for warehouse in Arad on 22.04.2020
        mockMvc.perform(get("/cc_arad/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        //2.Check how many Manifests in TPA with ID 4 (TPA from ARAD on 15:00 27.04.2020)
        mockMvc.perform(get("/tpa/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(2)));

        //3.Check how many TTT in XD STD Warehouse on 27.04.2020 before TTT in CC removed
        mockMvc.perform(get("/xd_std/ttt/2020-04-27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //4.Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();

        //5.Perform deletion of one of the TTT with id 5 name: ABSD01
        mockMvc.perform(delete("/ttt/delete/5"))
                .andDo(print())
                .andExpect(status().isOk());

        //6.Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();

        //7.Assertion to compare before and after deletion,
        // The QTy should be the same - deleted TTT from CC and created new one in XD
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest);

        //8.Check how many TTT remains in Arad Warehouse on 21.04.2020
        mockMvc.perform(get("/cc_arad/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //9.Check how many TTT in XD STD Warehouse on 27.04.2020 after TTT in CC removed
        mockMvc.perform(get("/xd_std/ttt/2020-04-27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        //10.Check how many Manifests in TPA for warehouse in Arad on 21.04.2020 after TTT was removed
        mockMvc.perform(get("/tpa/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));
    }

    /**
     * Test set for checking if removing of the particular TTT for CC Warehouse works properly.
     * (Case when Manifests are being delivered from CC to TXD directly.)
     * When user removes TTT in any CC all manifest from this TTT should be moved to new TTT in the TXD with arrival date
     * the same as date in the TTT which used to deliver each manifest from CC to XD. The old TTT in XD will not contain any
     * manifests from removed TTT in CC.
     *
     * @throws Exception exception for mockMvc
     */
    @Test
    public void deleteTttFromCcTestWithChangingTttSetInTXD() throws Exception {
        //1.Check how many TTTs for warehouse in Swiebodzice on 21.04.2020
        mockMvc.perform(get("/cc_swie/ttt/2020-04-21"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //2.Check how many Manifests in TPA with ID 2 (TPA from Swiebodzice on 2020-04-22 05:00)
        mockMvc.perform(get("/tpa/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));

        //3.Check how many TTT in TXD GRO Warehouse on 28.04.2020 before TTT in CC removed
        mockMvc.perform(get("/xd_gro/ttt/2020-04-28"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        //4.Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();

        //5.Perform deletion of one of the TTT with id 3 name: BART01
        mockMvc.perform(delete("/ttt/delete/3"))
                .andDo(print())
                .andExpect(status().isOk());

        //6.Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();
        //7.Assertion to compare before and after deletion,
        // The QTy should be the same - deleted TTT from CC and created new one in TXD
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest);

        //8.Check how many TTT remains in Swiebodzice Warehouse on 21.04.2020
        mockMvc.perform(get("/cc_swie/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //9.Check how many TTT in TXD Gro Warehouse on 28.04.2020 after TTT in CC removed
        mockMvc.perform(get("/xd_gro/ttt/2020-04-28"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        //10.Check how many Manifests in TPA for warehouse Swiebodzice on 2020-04-22 after TTT was removed
        mockMvc.perform(get("/tpa/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']").doesNotExist());
    }

    /**
     * Test case when user tries to delete TTT from XD and the TTT contains Manifest which goes from CC to XD and later
     * directly to plant(customer). If TTT has any manifest of such kind the system should decline the Delete operation.
     *
     * @throws Exception exception for mockMvc
     */
    @Test
    public void deleteTttFromXdTestCcXdCustomer() throws Exception {
        mockMvc.perform(delete("/ttt/delete/15"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("Message","TTT could not be deleted. Check Manifests from this TTT"));
    }

    /**
     * The test case when user tries to delete TTT in XD which contains 2 manifests:
     * 1. Supplier -> CC -> XD -> TXD -> Customer
     * Both of them will be departure from XD with different TPA. The 1st TPA has 2 manifest the second one - 1 manifest
     * When the TTT will be deleted from XD next changes should be provided:
     *  - TTT EXT1 should be deleted from XD;
     *  - TTT with truckName EXT1 should be created in TXD for date 2020-05-14T13:13;
     *  - TTT with truckName EXT1 should be created in TXD for date 2020-05-13T07:07;
     *  - The manifests feom old EXT1 TTT should be deleted from appropriate TPA's from the Same warehouse;
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromXdTestCcXdTxdAndXdTxd() throws Exception {
        //1. Check how many TTTs for warehouse in Stadthagen on 10.05.2020
        mockMvc.perform(get("/xd_std/ttt/2020-05-10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.truckName == \"ABC02\")]").exists())
                .andExpect(jsonPath("$[?(@.truckName == \"EXT1\")]").exists())
                .andExpect(jsonPath("$[?(@.truckName == \"NN001\")]").exists());

        //2. Check how many Manifests in TPA with ID 12=GRO-X (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/tpa/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(2)));

        //3.Check how many TTT in TXD GRO Warehouse on 14.05.2020 before TTT in CC removed
        mockMvc.perform(get("/xd_gro/ttt/2020-05-14"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        //4.Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();

        //5.Perform deletion of one of the TTT with id 16 name: EXT1
        mockMvc.perform(delete("/ttt/delete/16"))
                .andDo(print())
                .andExpect(status().isOk());

        //6.Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();
        //7.Assertion to compare before and after deletion,
        // The QTy should be the same - deleted TTT from CC and created new 2 TTT (EXT1 with date 14.05.2020 and EXT1 with date 13.05.2020)
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest + 1);

        //8.Check how many TTT remains in Stadthagen on 10.05.2020
        mockMvc.perform(get("/xd_std/ttt/2020-05-10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.truckName == \"ABC02\")]").exists())
                .andExpect(jsonPath("$[?(@.truckName == \"NN001\")]").exists());

        //9a.Check how many TTT in TXD GRO Warehouse on 14.05.2020 after TTT EXT1 removed in XD
        mockMvc.perform(get("/xd_gro/ttt/2020-05-14"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.truckName == \"EXT1\" && @.tttArrivalDatePlan == \"2020-05-14T13:13\")]").exists());
        //9b.Check how many TTT in TXD GRO Warehouse on 13.05.2020 after TTT EXT1 removed in XD
        mockMvc.perform(get("/xd_gro/ttt/2020-05-13"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.truckName == \"EXT1\" && @.tttArrivalDatePlan == \"2020-05-13T07:07\")]").exists());

        //10a. Check how many Manifests in TPA with ID 12=GRO-X after TTT was deleted (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/tpa/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));

        //10b. Check how many Manifests in TPA with ID 17=GRO-X after TTT was deleted (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/tpa/17"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']").doesNotExist());
    }

    /**
     * Test of attempt to delete TTT from the TXD Warehouse and all manifests from this TTT don't have any TPA except of
     * TPA which used by ManifestReference to be dispatched from TXD. After the TTT will be deleted the ManifestReferences
     * from this TTT should be deleted from Their TPA
     *
     * @throws Exception for mockMvc.
     */
    @Test
    public void deleteTttFromTxdStatus200() throws Exception{
        //1. Check the size of manifestReferenceSet in TPA 24 before TTT 21 removing
        mockMvc.perform(get("/tpa/24"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestReferenceSet']", hasSize(2)));

        //2. Removing TTT 24 from DB
        mockMvc.perform(delete("/ttt/delete/21"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message","TTT with id=21 was successfully removed."));

        //3. Check the size of manifestReferenceSet in TPA 24 after TTT 21 removing
        mockMvc.perform(get("/tpa/24"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestReferenceSet']", hasSize(1)));
    }

    /**
     * Test of attempt to Delete TTT from TXD warehouse when manifests from this TTT have TPAs in other warehouses.
     * Should return status 400 and message in http headers.
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromTxdResponse400() throws Exception{
        mockMvc.perform(delete("/ttt/delete/27"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("Message","TTT could not be deleted. Check Manifests from this TTT"));
    }

    /**
     * Test of attempt to Delete TTT from TXD which doesn't exist
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromTxdResponse404() throws Exception {
        mockMvc.perform(delete("/ttt/delete/127"))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Message","TTT Not Found"));
    }

    /**
     * Test of attempt to Delete TTT from TXD with status Arrive
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromTxdResponse422() throws Exception {
        mockMvc.perform(delete("/ttt/delete/28"))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Message","TTT with id=28 has status Arrived"));
    }

    /**
     * Test when user tries to delete TTT which doesn't exist
     * @throws Exception exception for mockMvc Exception
     */
    @Test
    public void deleteTttNotFoundCaseTest() throws Exception {
        //Perform deletion of one of the TTT with id 1000 which not exists
        mockMvc.perform(delete("/ttt/delete/1000"))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * Test when user tries to delete TTT which has status ARRIVED
     * @throws Exception exception for mockMvc Exception
     */
    @Test
    public void deleteTttUnprocessableEntityCaseTest() throws Exception {
        mockMvc.perform(delete("/ttt/delete/1"))
                .andDo(print())
                .andExpect(status().is(422));
    }

    @Test
    public void updateTttStatusOk() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TruckTimeTable truckTimeTable = entityManager.find(TruckTimeTable.class, 2L);//truckService.getTttService().getTttById(2L);
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();

        //Next call initialize the Set of Manifest in order to avoid LazyLoadException
        manifestSet.iterator();

        //Detach Entity to avoid saving the new information in DB 
        entityManager.detach(truckTimeTable);

        truckTimeTable.setTruckName("New_Name");
        truckTimeTable.setTttArrivalDatePlan("2020-05-30T13:00");
        String json = om.writeValueAsString(truckTimeTable);
        entityManager.getTransaction().commit();
        entityManager.close();

        //Check that Old Name of TTT remains in DB after the previous Transaction session committed.
        Assert.assertEquals("TPA3", truckService.getTttService().getTttById(2L).getTruckName());

        this.mockMvc.perform(put("/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200));

        mockMvc.perform(get("/ttt/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckName").value("New_Name"))
                .andExpect(jsonPath("$.tttArrivalDatePlan").value("2020-05-30T13:00"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }

}
