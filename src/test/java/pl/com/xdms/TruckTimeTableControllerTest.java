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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.ManifestService;
import pl.com.xdms.service.WarehouseManifestService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.excel.ExcelManifestReferenceService;
import pl.com.xdms.service.truck.TruckService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private ExcelManifestReferenceService excelManifestReferenceService;

    @Autowired
    private ManifestReferenceService manifestReferenceService;

    @Autowired
    private WarehouseManifestService warehouseManifestService;

    @Autowired
    private ManifestService manifestService;

    private TruckTimeTable newTtt;

    @Before
    public void init() {
        newTtt = new TruckTimeTable();
        newTtt.setTruckName("TPA_test");
    }

    @Test
    public void getAllTttByTheWarehouseAndCertainDate() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/ttt/2020-05-20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void getAllTttByTheWarehouseAndCertainDateWrongRegex() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/ttt/2020-04-233"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTttByIdTest() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/ttt/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckName").value("TPA1"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }

    @Test
    public void getTttByIdNotFound() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/ttt/101"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void createNewTtt() throws Exception {
        newTtt.setWarehouse(warehouseService.getWarehouseById(1L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/warehouse/cc_swie/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.truckName").value("TPA_test"))
                .andExpect(jsonPath("$.['tttStatus'].tttStatusName").value("PENDING"));
    }

    @Test
    public void createNewTttStatusDelayed() throws Exception {
        newTtt.setWarehouse(warehouseService.getWarehouseById(1L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().minusDays(5).withSecond(0).withNano(0).toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/warehouse/cc_swie/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.truckName").value("TPA_test"))
                .andExpect(jsonPath("$.['tttStatus'].tttStatusName").value("DELAYED"));
    }

    /**
     * When user sends TTT with warehouse which Id is another then current warehouse.
     * In this case Current warehouse will be added to this TTT and it will be saved if no other conditions are corrupted.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void createNewTttWithNotExistingWarehouse() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseID(100L);
        newTtt.setWarehouse(warehouse);
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/warehouse/cc_swie/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.['warehouse'].warehouseID").value(1));
    }

    /**
     * Warehouse with url "cc_swieh" doesn't exist
     */
    @Test
    public void createNewTttWithNullWarehouse() throws Exception {
        newTtt.setWarehouse(null);
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/warehouse/cc_swieh/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
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
            mockMvc.perform(get("/warehouse/cc_swie/ttt/2020-04-21"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/warehouse/cc_irun/ttt/2020-04-21"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/warehouse/cc_arad/ttt/2020-04-22"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        TTT_FOR_XD:
        {
            mockMvc.perform(get("/warehouse/xd_std/ttt/2020-04-23"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/warehouse/xd_std/ttt/2020-04-27"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

        }

        TTT_FOR_TXD:
        {
            mockMvc.perform(get("/warehouse/xd_gro/ttt/2020-04-28"))
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
     * Case Manifest goes: Supplier -> CC -> XD -> customer
     *
     * @throws Exception exception for mockMvc
     */
    @Test
    public void deleteTttFromCcTestWithChangingTttSetInXD() throws Exception {
        //1.Check how many TTTs for warehouse in Arad on 22.04.2020
        mockMvc.perform(get("/warehouse/cc_arad/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        //2.Check how many Manifests in TPA with ID 4 (TPA from ARAD on 15:00 27.04.2020) /cc_arad
        mockMvc.perform(get("/warehouse/cc_arad/tpa/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(2)));

        //3.Check how many TTT in XD STD Warehouse on 27.04.2020 before TTT in CC removed
        mockMvc.perform(get("/warehouse/xd_std/ttt/2020-04-27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //4.Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();
        TruckTimeTable ttt = truckService.getTttService().getTttById(5L);
        //4a Check qty of WarehouseManifest entities in DB for TTT=5 Before Request.
        Assert.assertEquals(1, warehouseManifestService.findAllByTtt(ttt).size());
        //4b Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=4 and Manifest=5 Before Request
        Assert.assertEquals(new Long(8L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(4L), manifestService.findManifestById(5L)).getTtt().getTttID());


        //5.Perform deletion of TTT with id 5 name: ABSD01
        mockMvc.perform(delete("/warehouse/cc_arad/ttt/delete/5"))
                .andDo(print())
                .andExpect(status().isOk());

        //6.Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();

        //6.a Check qty of WarehouseManifest entities in DB for TTT=5 After Request.
        Assert.assertEquals(0, warehouseManifestService.findAllByTtt(ttt).size());

        //6b. Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=4 and Manifest=5 Before Request
        Assert.assertEquals(new Long(33), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(4L), manifestService.findManifestById(5L)).getTtt().getTttID());

        //7.Assertion to compare before and after deletion,
        // The QTy should be the same - deleted TTT from CC and created new one in XD
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest);

        //8.Check how many TTT remains in Arad Warehouse on 21.04.2020
        mockMvc.perform(get("/warehouse/cc_arad/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //9.Check how many TTT in XD STD Warehouse on 27.04.2020 after TTT in CC removed
        mockMvc.perform(get("/warehouse/xd_std/ttt/2020-04-27"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        //10.Check how many Manifests in TPA for warehouse in Arad on 21.04.2020 after TTT was removed /cc_irun
        mockMvc.perform(get("/warehouse/cc_arad/tpa/4"))
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
        mockMvc.perform(get("/warehouse/cc_swie/ttt/2020-04-21"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //2.Check how many Manifests in TPA with ID 2 (TPA from Swiebodzice on 2020-04-22 05:00)
        mockMvc.perform(get("/warehouse/cc_swie/tpa/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));

        //3.Check how many TTT in TXD GRO Warehouse on 28.04.2020 before TTT in CC removed
        mockMvc.perform(get("/warehouse/xd_gro/ttt/2020-04-28"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        //4.Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();

        TruckTimeTable ttt = truckService.getTttService().getTttById(3L);

        //4a Check qty of WarehouseManifest entities in DB for TTT=3 Before Request.
        Assert.assertEquals(1, warehouseManifestService.findAllByTtt(ttt).size());

        //4b Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=2 and Manifest=3 Before Request
        Assert.assertEquals(new Long(9L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(2L), manifestService.findManifestById(3L)).getTtt().getTttID());

        //5.Perform deletion of one of the TTT with id 3 name: BART01
        mockMvc.perform(delete("/warehouse/cc_swie/ttt/delete/3"))
                .andDo(print())
                .andExpect(status().isOk());

        //6.Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();

        //6.a Check qty of WarehouseManifest entities in DB for TTT=3 After Request.
        Assert.assertEquals(0, warehouseManifestService.findAllByTtt(ttt).size());

        //6b. Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=2 and Manifest=3 Before Request
        Assert.assertEquals(new Long(34L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(2L), manifestService.findManifestById(3L)).getTtt().getTttID());

        //7.Assertion to compare before and after deletion,
        // The QTy should be the same - deleted TTT from CC and created new one in TXD
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest);

        //8.Check how many TTT remains in Swiebodzice Warehouse on 21.04.2020
        mockMvc.perform(get("/warehouse/cc_swie/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //9.Check how many TTT in TXD Gro Warehouse on 28.04.2020 after TTT in CC removed
        mockMvc.perform(get("/warehouse/xd_gro/ttt/2020-04-28"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        //10.Check how many Manifests in TPA for warehouse Swiebodzice on 2020-04-22 after TTT was removed
        mockMvc.perform(get("/warehouse/cc_swie/tpa/2"))
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
        mockMvc.perform(delete("/warehouse/xd_std/ttt/delete/15"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("Error:", "TTT could not be deleted. Check Manifests from this TTT"));
    }

    /**
     * The test case when user tries to delete TTT in XD which contains 2 manifests:
     * 1. Supplier -> CC -> XD -> TXD -> Customer
     * Both of them will be departure from XD with different TPA. The 1st TPA has 2 manifests the second one - 1 manifest
     * When the TTT will be deleted from XD next changes should be provided:
     * - TTT EXT1 should be deleted from XD;
     * - TTT with truckName EXT1 should be created in TXD for date 2020-05-14T13:13;
     * - TTT with truckName EXT1 should be created in TXD for date 2020-05-13T07:07;
     * - The manifests from old EXT1 TTT should be deleted from appropriate TPA's from the Same warehouse;
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromXdTestCcXdTxdAndXdTxd() throws Exception {
        //1. Check how many TTTs for warehouse in Stadthagen on 10.05.2020
        mockMvc.perform(get("/warehouse/xd_std/ttt/2020-05-10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.truckName == \"ABC02\")]").exists())
                .andExpect(jsonPath("$[?(@.truckName == \"EXT1\")]").exists())
                .andExpect(jsonPath("$[?(@.truckName == \"NN001\")]").exists());

        //2a. Check how many Manifests in TPA with ID 12=GRO-X (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/warehouse/xd_std/tpa/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(2)));

        //2b. Check how many Manifests in TPA with ID 17=GRO-X2 (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/warehouse/xd_std/tpa/17"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));

        //3.Check how many TTT in TXD GRO Warehouse on 14.05.2020 before TTT in CC removed
        mockMvc.perform(get("/warehouse/xd_gro/ttt/2020-05-14"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        //4.Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();

        TruckTimeTable ttt = truckService.getTttService().getTttById(16L);
        //4a Check qty of WarehouseManifest entities in DB for TTT=3 Before Request.
        Assert.assertEquals(2, warehouseManifestService.findAllByTtt(ttt).size());

        //4b Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=4 and Manifest=7 Before Request
        Assert.assertEquals(new Long(16L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(4L), manifestService.findManifestById(7L)).getTtt().getTttID());
        Assert.assertEquals(new Long(16L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(4L), manifestService.findManifestById(13L)).getTtt().getTttID());

        //5.Perform deletion of one of the TTT with id 16 name: EXT1
        mockMvc.perform(delete("/warehouse/xd_std/ttt/delete/16"))
                .andDo(print())
                .andExpect(status().isOk());

        //6.Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();

        //6.a Check qty of WarehouseManifest entities in DB for TTT=16 After Request.
        Assert.assertEquals(0, warehouseManifestService.findAllByTtt(ttt).size());

        //6b. Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=2 and Manifest=7 and 13 Before Request
        Assert.assertEquals(new Long(31L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(2L), manifestService.findManifestById(7L)).getTtt().getTttID());
        Assert.assertEquals(new Long(30L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(2L), manifestService.findManifestById(13L)).getTtt().getTttID());

        //7.Assertion to compare before and after deletion,
        // The QTy should be the same - deleted TTT from CC and created new 2 TTT (EXT1 with date 14.05.2020 and EXT1 with date 13.05.2020)
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest + 1);

        //8.Check how many TTT remains in Stadthagen on 10.05.2020
        mockMvc.perform(get("/warehouse/xd_std/ttt/2020-05-10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.truckName == \"ABC02\")]").exists())
                .andExpect(jsonPath("$[?(@.truckName == \"NN001\")]").exists());

        //9a.Check how many TTT in TXD GRO Warehouse on 14.05.2020 after TTT EXT1 removed in XD
        mockMvc.perform(get("/warehouse/xd_gro/ttt/2020-05-14"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.truckName == \"EXT1\" && @.tttArrivalDatePlan == \"2020-05-14T13:13\")]").exists());

        //9b.Check how many TTT in TXD GRO Warehouse on 13.05.2020 after TTT EXT1 removed in XD
        mockMvc.perform(get("/warehouse/xd_gro/ttt/2020-05-13"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.truckName == \"EXT1\" && @.tttArrivalDatePlan == \"2020-05-13T07:07\")]").exists());

        //10a. Check how many Manifests in TPA with ID 12=GRO-X after TTT was deleted (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/warehouse/xd_std/tpa/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));

        //10b. Check how many Manifests in TPA with ID 17=GRO-X after TTT was deleted (TPA from Stadthagen on 2020-05-11T14:30)
        mockMvc.perform(get("/warehouse/xd_std/tpa/17"))
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
    public void deleteTttFromTxdStatus200() throws Exception {
        //1. Check the size of manifestReferenceSet in TPA 24 before TTT 21 removing
        mockMvc.perform(get("/warehouse/xd_gro/tpa/24"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestReferenceSet']", hasSize(2)));

        TruckTimeTable ttt = truckService.getTttService().getTttById(21L);
        //1a Check qty of WarehouseManifest entities in DB for TTT=21 Before Request.
        Assert.assertEquals(1, warehouseManifestService.findAllByTtt(ttt).size());

        //1 Check ID of the TTT which is assigned to WarehouseManifest entity for Warehouse=2 and Manifest=15 Before Request
        Assert.assertEquals(new Long(21L), warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(2L), manifestService.findManifestById(15L)).getTtt().getTttID());

        //2. Removing TTT 24 from DB
        mockMvc.perform(delete("/warehouse/xd_gro/ttt/delete/21"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "TTT with id=21 was successfully removed."));

        //3a Check qty of WarehouseManifest entities in DB for TTT=21 After Request.
        Assert.assertEquals(0, warehouseManifestService.findAllByTtt(ttt).size());

        //3b. Check is there any TTT which is assigned to WarehouseManifest entity for Warehouse=2 and Manifest=15 After Request
        Assert.assertNull( warehouseManifestService.findByWarehouseAndManifest(warehouseService.getWarehouseById(2L), manifestService.findManifestById(15L)));

        //3c Check the size of manifestReferenceSet in TPA 24 after TTT 21 removing
        mockMvc.perform(get("/warehouse/xd_gro/tpa/24"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestReferenceSet']", hasSize(1)));
    }

    /**
     * Test of attempt to Delete TTT from TXD warehouse when manifests from this TTT have TPAs in other warehouses.
     * Should return status 400 and message in http headers.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromTxdResponse400() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_gro/ttt/delete/27"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("Error:", "TTT could not be deleted. Check Manifests from this TTT"));
    }

    /**
     * Test of attempt to Delete TTT from TXD which doesn't exist
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromTxdResponse404() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_gro/ttt/delete/127"))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "TTT Not Found"));
    }

    /**
     * Test of attempt to Delete TTT from TXD with status Arrive
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void deleteTttFromTxdResponse422() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_gro/ttt/delete/28"))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Message:", "TTT with id=28 has status Arrived"));
    }

    /**
     * Test when user tries to delete TTT which doesn't exist
     *
     * @throws Exception exception for mockMvc Exception
     */
    @Test
    public void deleteTttNotFoundCaseTest() throws Exception {
        //Perform deletion of one of the TTT with id 1000 which does not exist
        mockMvc.perform(delete("/warehouse/xd_gro/ttt/delete/1000"))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * Test when user tries to delete TTT which has status ARRIVED
     *
     * @throws Exception exception for mockMvc Exception
     */
    @Test
    public void deleteTttUnprocessableEntityCaseTest() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_swie/ttt/delete/1"))
                .andDo(print())
                .andExpect(status().is(422));
    }

    /**
     * Check the Attempt of Updating the TTT. Status 200 should be returned.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTttStatusOk200() throws Exception {
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
        truckTimeTable.setTttArrivalDatePlan("2030-05-30T13:00");
        String json = om.writeValueAsString(truckTimeTable);
        entityManager.getTransaction().commit();
        entityManager.close();

        //Check that Old Name of TTT remains in DB after the previous Transaction session committed.
        Assert.assertEquals("TPA3", truckService.getTttService().getTttById(2L).getTruckName());

        mockMvc.perform(put("/warehouse/cc_swie/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "TTT with id=2 was successfully updated"));

        mockMvc.perform(get("/warehouse/cc_swie/ttt/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckName").value("New_Name"))
                .andExpect(jsonPath("$.tttArrivalDatePlan").value("2030-05-30T13:00"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }

    /**
     * Case when given TTT has id which doesn't exist in DB.
     * Status 404 should be returned
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTttStatus404WrongId() throws Exception {
        ObjectMapper om = new ObjectMapper();
        newTtt.setTttID(1000L);
        String json = om.writeValueAsString(newTtt);

        mockMvc.perform(put("/warehouse/cc_swie/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "TTT with id=1000 not found, returning error"));
    }

    /**
     * Case when given TTT has null value for ID
     * Status 404 should be returned
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTttStatus404NullId() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(newTtt);

        mockMvc.perform(put("/warehouse/cc_swie/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "Not Existing"));
    }

    /**
     * Case when user tries to update TTT by providing the Arrival Date Plan which is in the Past
     * Status 422 should be returned
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTttStatus422EtaInThePast() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TruckTimeTable truckTimeTable = entityManager.find(TruckTimeTable.class, 2L);//truckService.getTttService().getTttById(2L);
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();

        //Next call initialize the Set of Manifest in order to avoid LazyLoadException
        manifestSet.iterator();

        //Detach Entity to avoid saving the new information in DB
        entityManager.detach(truckTimeTable);

        truckTimeTable.setTruckName("New_NAME");
        truckTimeTable.setTttArrivalDatePlan("2020-05-05T13:00");
        String json = om.writeValueAsString(truckTimeTable);
        entityManager.getTransaction().commit();
        entityManager.close();

        //Check that Old Name of TTT remains in DB after the previous Transaction session committed.
        Assert.assertEquals("TPA3", truckService.getTttService().getTttById(2L).getTruckName());

        mockMvc.perform(put("/warehouse/cc_swie/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Error:", "TTT id=2 has status ARRIVED or ETA date is in the Past"));
    }

    /**
     * Case when user tries to update TTT which has status ARRIVED.
     * Status 422 should be returned
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTttStatus422TttArrived() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TruckTimeTable truckTimeTable = entityManager.find(TruckTimeTable.class, 28L);//(28, 'BRA1-II',  '2020-05-12T12:00', 3, 2);
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();

        //Next call initialize the Set of Manifest in order to avoid LazyLoadException
        manifestSet.iterator();

        //Detach Entity to avoid saving the new information in DB
        entityManager.detach(truckTimeTable);

        truckTimeTable.setTruckName("New_NAME");
        truckTimeTable.setTttArrivalDatePlan("2030-05-05T13:00");
        String json = om.writeValueAsString(truckTimeTable);
        entityManager.getTransaction().commit();
        entityManager.close();

        //Check that Old Name of TTT remains in DB after the previous Transaction session committed.
        Assert.assertEquals("TPA3", truckService.getTttService().getTttById(2L).getTruckName());

        mockMvc.perform(put("/warehouse/xd_gro/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().stringValues("Error:", "TTT id=28 has status ARRIVED or ETA date is in the Past"));
    }

    /**
     * Check the attempt of updating the TTT with Name and ArrivalPlan in wrong format. Status 412 should be returned.
     *
     * @throws Exception for mockMvc
     */
    @Test
    public void updateTttStatus412() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TruckTimeTable truckTimeTable = entityManager.find(TruckTimeTable.class, 2L);//truckService.getTttService().getTttById(2L);
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();

        //Next call initialize the Set of Manifest in order to avoid LazyLoadException
        manifestSet.iterator();

        //Detach Entity to avoid saving the new information in DB
        entityManager.detach(truckTimeTable);

        truckTimeTable.setTruckName("NAME!");
        truckTimeTable.setTttArrivalDatePlan("2030-05-XXT13:00");
        String json = om.writeValueAsString(truckTimeTable);
        entityManager.getTransaction().commit();
        entityManager.close();

        //Check that Old Name of TTT remains in DB after the previous Transaction session committed.
        Assert.assertEquals("TPA3", truckService.getTttService().getTttById(2L).getTruckName());

        mockMvc.perform(put("/warehouse/cc_swie/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("truckTimeTable-tttArrivalDatePlan_Pattern", "must match \"^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9](:[0-5][0-9])?$\""));
    }

    /**
     * Test of creation .xlsx file with information about ManifestReferences in TTT for making reception.
     */
    @Test
    public void getFileForReceptionTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/warehouse/xd_gro/ttt/10/reception.xlsx").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        File tempFile = File.createTempFile("test", ".xlsx", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(result.getResponse().getContentAsByteArray());
        log.info("Result {}", tempFile.getAbsolutePath());

        //Map of the ManifestReference from received file after get request.
        Map<Long, ManifestReference> testFileMap = excelManifestReferenceService.readExcel(tempFile);
        //List of the ManifestReference from received testFileMap.
        List<ManifestReference> manifestReferenceList = new ArrayList<>(testFileMap.values());
        //List of ManifestReference created with key set from testFileMap
        List<ManifestReference> referenceList = manifestReferenceService.getManRefListWithinIdSet(testFileMap.keySet());
        Comparator<ManifestReference> comparator = Comparator.comparing(ManifestReference::getManifestReferenceId);
        referenceList.sort(comparator);
        manifestReferenceList.sort(comparator);
        Assert.assertEquals(referenceList.stream().map(ManifestReference::getManifestReferenceId).collect(Collectors.toList()),
                manifestReferenceList.stream().map(ManifestReference::getManifestReferenceId).collect(Collectors.toList()));
    }

    /**
     * upload file with reception information
     */
    @Test
    public void uploadFileWithReceptionInformation() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("excelTests/reception_test.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file.toPath()));

        ManifestReference manifestReference3 = manifestReferenceService.findById(3L);
        ManifestReference manifestReference2 = manifestReferenceService.findById(2L);

        Assert.assertNull(manifestReference3.getReceptionNumber());
        Assert.assertNull(manifestReference2.getReceptionNumber());

        mockMvc.perform(multipart("/warehouse/xd_gro/ttt/10/uploadFile").file(mockMultipartFile))///warehouse/xd_gro/ttt/10
                .andDo(print())
                .andExpect(status().isOk());

        ManifestReference manifestReference3After = manifestReferenceService.findById(3L);
        ManifestReference manifestReference2After = manifestReferenceService.findById(2L);

        Assert.assertNotNull(manifestReference3After.getReceptionNumber());
        Assert.assertNotNull(manifestReference2After.getReceptionNumber());
    }
}
