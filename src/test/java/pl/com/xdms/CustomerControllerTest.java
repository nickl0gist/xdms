package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.WhCustomerService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 26.11.2019
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
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private WhCustomerService whCustomerService;
    @Autowired
    private WarehouseService warehouseService;

    private Customer newCustomer;

    @Before
    public void init(){
        newCustomer = new Customer();
        newCustomer.setName("NewCustomer");
        newCustomer.setCustomerCode("NEW_CUST");
        newCustomer.setIsActive(true);
        newCustomer.setCity("Belgrad");
        newCustomer.setCountry("BO");
        newCustomer.setEmail("some@email.bo");
        newCustomer.setPostCode("200188");
        newCustomer.setStreet("Korowkina 22");
    }

    @Test
    public void getAllCustomers() throws Exception{
        mockMvc.perform(get("/admin/customers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    public void getCustomerById() throws Exception{
        mockMvc.perform(get("/admin/customers/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCode").value("123123"))
                .andExpect(jsonPath("$.postCode").value("250250"));
    }

    @Test
    public void getCustomerByIdNotFound() throws Exception {
        int id = 50;
        mockMvc.perform(get("/admin/customers/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void getOnlyActiveCustomers() throws Exception {
        mockMvc.perform(get("/admin/customers/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void getOnlyNotActiveCustomers() throws Exception {
        mockMvc.perform(get("/admin/customers/not_active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void getCustomersOrderedByNameAsc() throws Exception {
        mockMvc.perform(get("/admin/customers/ordered_by/name/asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].customerID").value(4))
                .andExpect(jsonPath("$[4].customerID").value(2));
    }

    @Test
    public void getCustomersOrderedByVendorCodeDesc() throws Exception {
        mockMvc.perform(get("/admin/customers/ordered_by/customer_code/desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].customerID").value(3))
                .andExpect(jsonPath("$[4].customerID").value(1));
    }

    @Test
    public void getCustomersBySearch() throws Exception {
        String search = "ArgentinaName";
        mockMvc.perform(get("/admin/customers/search/" + search))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerID").value(4));
    }

    @Test
    public void updateCustomerTestStatusOk() throws Exception {
        Long id = 1L;
        Customer customer = customerService.getCustomerById(id);
        customer.setIsActive(false);
        customer.setCustomerCode("0000000");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(customer);
        this.mockMvc.perform(put("/admin/customers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.customerCode").value("0000000"));
    }

    @Test
    public void updateCustomerTestStatusBadEntity() throws Exception {
        Long id = 1L;
        Customer customer = customerService.getCustomerById(id);
        customer.setName("");
        customer.setCustomerCode("WE12-344/");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(customer);
        this.mockMvc.perform(put("/admin/customers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("customer-customerCode_Pattern"))
                .andExpect(header().exists("customer-name_Size"))
                .andExpect(header().exists("customer-name_NotBlank"));
    }

    @Test
    public void updateCustomerWhichNotExisted() throws Exception {
        newCustomer.setCustomerID(10L);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newCustomer);
        this.mockMvc.perform(put("/admin/customers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404));
    }

    @Test
    public void createCustomerTestStatusOK() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newCustomer);

        this.mockMvc.perform(post("/admin/customers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));

        Warehouse warehouse1 = warehouseService.getWarehouseById(3L);
        Warehouse warehouse2 = warehouseService.getWarehouseById(4L);
        Warehouse warehouse3 = warehouseService.getWarehouseById(5L);

        Assert.assertEquals(1, whCustomerService.getAllWhCustomersByWarehouseNotActive(warehouse1).size());
        Assert.assertEquals(1, whCustomerService.getAllWhCustomersByWarehouseNotActive(warehouse2).size());
        Assert.assertEquals(1, whCustomerService.getAllWhCustomersByWarehouseNotActive(warehouse3).size());
        Assert.assertEquals(0, whCustomerService.getAllWhCustomersByWarehouseIsActive(warehouse1).size());
        Assert.assertEquals(0, whCustomerService.getAllWhCustomersByWarehouseIsActive(warehouse2).size());
        Assert.assertEquals(0, whCustomerService.getAllWhCustomersByWarehouseIsActive(warehouse3).size());
    }

    @Test
    public void createCustomerTestStatusBadEntity() throws Exception {
        newCustomer.setName("043");
        newCustomer.setCustomerCode(null);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newCustomer);

        this.mockMvc.perform(post("/admin/customers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().exists("customer-customerCode_NotBlank"))
                .andExpect(header().exists("customer-customerCode_NotNull"))
                .andExpect(header().exists("customer-name_Size"));
    }

    private String jsonClean(String json) {
        return json.replaceAll("^ +| +$|\\R |, +|\\{ ", "")
                .replace(" : ", ":")
                .replaceAll("   \"| \"", "\"")
                .replaceAll("\\r\\n?|\\n", "")
                .replace(" }", "}");
    }
}
