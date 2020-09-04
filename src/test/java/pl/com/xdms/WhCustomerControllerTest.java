package pl.com.xdms;

import lombok.extern.slf4j.Slf4j;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 25.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = {"/sql_scripts/createValuesInDBforTests.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class WhCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllWarehousesTest() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/customers"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    public void getAllWarehousesTestNotFound() throws Exception {
        mockMvc.perform(get("/warehouse/bla_bla/customers"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    /**
     * Get WhCustomer pairs by certain warehouse where the connection WarehouseCustomer isActive
     * @throws Exception
     */
    @Test
    public void getOnlyActiveWarehousesTest() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/customers/active"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    public void getOnlyInactiveWarehousesTest() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/customers/inactive"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void updateWhCustomerTestOkStatus() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/warehouse/cc_swie/customer/2"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.isActive").value(true)).andReturn();

        String jsonString = mvcResult.getResponse().getContentAsString();
        jsonString = jsonString.replace("02\"},\"isActive\":true", "02\"},\"isActive\":false");
        log.info(jsonString);
        mockMvc.perform(put("/warehouse/cc_swie").contentType(MediaType.APPLICATION_JSON_UTF8).content(jsonString))
                .andDo(print())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(status().isOk());
    }
}

