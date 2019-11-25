package pl.com.xdms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 25.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
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
        mockMvc.perform(get("/coordinator/warehouse/cc_swie/customers"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    public void getAllWarehousesTestNotFound() throws Exception {
        mockMvc.perform(get("/coordinator/warehouse/bla_bla/customers"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void getOnlyActiveWarehousesTest() throws Exception {
        mockMvc.perform(get("/coordinator/warehouse/cc_swie/customers/active"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void getOnlyInactiveWarehousesTest() throws Exception {
        mockMvc.perform(get("/coordinator/warehouse/cc_swie/customers/inactive"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
