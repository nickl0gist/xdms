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
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 12.04.2020
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private TruckService truckService;

    private TruckTimeTable newTtt;

    @Before
    public void init(){
        newTtt = new TruckTimeTable();
        newTtt.setTruckName("TPA_test");
    }

    @Test
    public void getAllTttByTheWarehouseAndCertainDate() throws Exception {
        mockMvc.perform(get("/cc_swie/ttt/2020-04-20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void getAllTttByTheWarehouseAndCertainDateWrongRegex() throws Exception {
        mockMvc.perform(get("/cc_swie/ttt/2020-04-233"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getTttByIdTest() throws Exception{
        mockMvc.perform(get("/ttt/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckName").value("TPA1"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }

    @Test
    public void getTttByIdNotFound() throws Exception{
        mockMvc.perform(get("/ttt/101"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void createNewTtt() throws Exception{
        newTtt.setWarehouse(warehouseService.getWarehouseById(1L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.truckName").value("TPA_test"))
                .andExpect(jsonPath("$.['tttStatus'].tttStatusName").value("PENDING"));;
    }

    @Test
    public void createNewTttStatusDelayed() throws Exception{
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
    public void createNewTttWithNotExistingWarehouse() throws Exception{

        newTtt.setWarehouse(warehouseService.getWarehouseById(100L));
        newTtt.setTttArrivalDatePlan(LocalDateTime.now().plusDays(5).withSecond(0).withNano(0).toString());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newTtt);

        mockMvc.perform(post("/ttt/create").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createNewTttWithNullWarehouse() throws Exception{
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
     * @throws Exception exception for mockMvc
     */
    @Test
    public void deleteTttFromCcTest() throws Exception{
        //Check how many TTTs for warehouse in Arad  on 21.04.2020
        mockMvc.perform(get("/cc_arad/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        //Check how many Manifests in TPA for warehouse in Arad on 21.04.2020
        mockMvc.perform(get("/tpa/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(2)));

        // Get qty of all TTTs in DB
        int qtyOfTttInDbBeforeRequest = truckService.getTttService().getAllTtt().size();

        //Perform deletion of one of the TTT with id 5
        mockMvc.perform(delete("/ttt/delete/5"))
                .andDo(print())
                .andExpect(status().isOk());

        // Get qty of all TTTs in DB after deletion was performed
        int qtyOfTttInDbAfterRequest = truckService.getTttService().getAllTtt().size();

        //Assertion to compare before and after deletion
        Assert.assertEquals(qtyOfTttInDbAfterRequest, qtyOfTttInDbBeforeRequest - 1);

        //Check how many TTT remains in Arad Warehouse on 21.04.2020
        mockMvc.perform(get("/cc_arad/ttt/2020-04-22"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));


        //Check how many Manifests in TPA for warehouse in Arad on 21.04.2020 after TTT was removed
        mockMvc.perform(get("/tpa/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['manifestSet']", hasSize(1)));

    }

    /**
     * Test when user tries to delete TTT which doesn't exist
     * @throws Exception exception for mockMvc Exception
     */
    @Test
    public void  deleteTttNotFoundCaseTest() throws Exception{
        //Perform deletion of one of the TTT with id 1000 which not exists
        mockMvc.perform(delete("/ttt/delete/1000"))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * Test when user tries to delete TTT which has manifest which has checked Pallet Real Qty
     * @throws Exception exception for mockMvc Exception
     */
    @Test
    public void  deleteTttUnprocessableEntityCaseTest() throws Exception{
        //Perform deletion of one of the TTT with id 1 which not has Manifest with Real Pallet Qty checked
        mockMvc.perform(delete("/ttt/delete/1"))
                .andDo(print())
                .andExpect(status().is(422));
    }

    @Test
    @Transactional // used to avoid LazyInitializationException which occurs while ManifestSet for TTT is loading.
    public void updateTttStatusOk() throws Exception{

        TruckTimeTable truckTimeTable = truckService.getTttService().getTttById(1L);
        truckTimeTable.setTruckName("New_Name");
        truckTimeTable.setTttArrivalDatePlan("2020-04-30T13:00");

        ObjectMapper om = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        //String json = ow.writeValueAsString(truckTimeTable);
        String json = om.writeValueAsString(truckTimeTable);

        this.mockMvc.perform(put("/ttt/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200));

        mockMvc.perform(get("/ttt/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckName").value("New_Name"))
                .andExpect(jsonPath("$.tttArrivalDatePlan").value("2020-04-30T13:00"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));

    }


}
