package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.SupplierService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 17.11.2019
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
public class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierService supplierService;

    private Supplier newSupplier;

    @Before
    public void init() {
        newSupplier = new Supplier();
        newSupplier.setIsActive(true);
        newSupplier.setCity("Doneck");
        newSupplier.setCountry("Ukraine");
        newSupplier.setEmail("mydoneck@ukraine.ua");
        newSupplier.setName("Donbass");
        newSupplier.setPostCode("85258");
        newSupplier.setVendorCode("305070");
        newSupplier.setStreet("Kujbyshewa 32");
    }

    @Test
    public void getAllSuppliersTest() throws Exception {
        mockMvc.perform(get("/coordinator/suppliers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    public void getSupplierByIdStatusOk() throws Exception {
        int id = 4;
        mockMvc.perform(get("/coordinator/suppliers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierID").value(4))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    public void getSupplierByIdNotFound() throws Exception {
        int id = 50;
        mockMvc.perform(get("/coordinator/suppliers/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void getOnlyActiveSuppliers() throws Exception {
        mockMvc.perform(get("/coordinator/suppliers/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void getOnlyNotActiveSuppliers() throws Exception {
        mockMvc.perform(get("/coordinator/suppliers/not_active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void getSuppliersOrderedByNameAsc() throws Exception {
        mockMvc.perform(get("/coordinator/suppliers/ordered_by/name/asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].supplierID").value(3))
                .andExpect(jsonPath("$[5].supplierID").value(5));
    }

    @Test
    public void getSuppliersOrderedByVendorCodeDesc() throws Exception {
        mockMvc.perform(get("/coordinator/suppliers/ordered_by/vendor_code/desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].supplierID").value(4))
                .andExpect(jsonPath("$[5].supplierID").value(1));
    }

    @Test
    public void getSuppliersBySearch() throws Exception {
        String search = "BCDEFG";
        mockMvc.perform(get("/coordinator/suppliers/search/" + search))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].supplierID").value(4));
    }

    @Test
    public void updateSupplierTestStatusOk() throws Exception {
        Long id = 1L;
        Supplier supplier = supplierService.getSupplierById(id);
        supplier.setIsActive(false);
        supplier.setVendorCode("0000000");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(supplier);
        this.mockMvc.perform(put("/coordinator/suppliers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.vendorCode").value("0000000"));
    }

    @Test
    public void updateSupplierTestStatusBadEntity() throws Exception {
        Long id = 1L;
        Supplier supplier = supplierService.getSupplierById(id);
        supplier.setName("");
        supplier.setVendorCode("WE12344");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(supplier);
        this.mockMvc.perform(put("/coordinator/suppliers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("supplier-vendorCode_Pattern"))
                .andExpect(header().exists("supplier-name_Size"))
                .andExpect(header().exists("supplier-name_NotBlank"));
    }

    @Test
    public void updateSupplierWhichNotExisted() throws Exception {
        newSupplier.setSupplierID(10L);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newSupplier);
        this.mockMvc.perform(put("/coordinator/suppliers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404));
    }

    @Test
    public void createSupplierTestStatusOK() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newSupplier);

        this.mockMvc.perform(post("/coordinator/suppliers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));
    }

    @Test
    public void createSupplierTestStatusBadEntity() throws Exception {
        newSupplier.setName("043");
        newSupplier.setVendorCode(null);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newSupplier);

        this.mockMvc.perform(post("/coordinator/suppliers").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().exists("supplier-vendorCode_NotBlank"))
                .andExpect(header().exists("supplier-vendorCode_NotNull"))
                .andExpect(header().exists("supplier-name_Size"));
    }

    private String jsonClean(String json) {
        return json.replaceAll("^ +| +$|\\R |, +|\\{ ", "")
                .replace(" : ", ":")
                .replaceAll("   \"| \"", "\"")
                .replaceAll("\\r\\n?|\\n", "")
                .replace(" }", "}");
    }
}
