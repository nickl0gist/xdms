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
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.ReferenceService;
import pl.com.xdms.service.StorageLocationService;
import pl.com.xdms.service.SupplierService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 07.11.2019
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
public class ReferenceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReferenceService referenceService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private StorageLocationService storageLocationService;

    private Reference newRef;

    @Before
    public void init(){
        newRef = new Reference();
        Customer customer = customerService.getCustomerById(1L);
        Supplier supplier = supplierService.getSupplierById(1L);
        StorageLocation storageLocation = storageLocationService.getStorageLocationById(1L);
        newRef.setCustomer(customer);
        newRef.setSupplier(supplier);
        newRef.setStorageLocation(storageLocation);
        newRef.setNumber("1234567890-XXX");
        newRef.setName("Reference Test Name");
        newRef.setHsCode("80908090");
        newRef.setWeight(50.50);
        newRef.setWeightOfPackaging(10);
        newRef.setStackability(2);
        newRef.setPuPerHU(100);
        newRef.setPcsPerPU(1000);
        newRef.setIsActive(true);
        newRef.setPalletWidth(1000);
        newRef.setPalletLength(800);
        newRef.setPalletHeight(1500);
        newRef.setPalletWeight(15);
        newRef.setDesignationEN("Description of Material");
        newRef.setDesignationRU("Описание Товара");
        newRef.setSupplierAgreement("550011223355");
        newRef.setCustomerAgreement("880088008800");
    }

    @Test
    public void getAllReferences() throws Exception {
        this.mockMvc.perform(get("/coordinator/references"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void searchReferencesByStringTest() throws Exception {
        String searchString = "11111XXX-CO";
        this.mockMvc.perform(get("/coordinator/references/search/" + searchString))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].referenceID").value(2));
    }

    @Test
    public void searchReferencesByStringTestNoResults() throws Exception {
        String searchString = "1111111111111";
        this.mockMvc.perform(get("/coordinator/references/search/" + searchString))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void getReferenceByIdTestStatusOk() throws Exception {
        int id = 1;
        this.mockMvc.perform(get("/coordinator/references/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceID").value(1));
    }

    @Test
    public void getReferenceByIdTestStatusNOk() throws Exception {
        int id = 100;
        this.mockMvc.perform(get("/coordinator/references/" + id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void getReferenceByIdTestString() throws Exception {
        String id = "string";
        this.mockMvc.perform(get("/coordinator/references/" + id))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void getOnlyActiveReference() throws Exception {
        mockMvc.perform(get("/coordinator/references/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void getOnlyNotActiveReference() throws Exception {
        mockMvc.perform(get("/coordinator/references/not_active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void updateReferenceTestStatusOk() throws Exception {
        Long id = 1L;
        Reference reference = referenceService.getRefById(id);
        reference.setName("12oooooo");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(reference);
        this.mockMvc.perform(put("/coordinator/references").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateReferenceTestStatusBadEntity() throws Exception {
        Long id = 1L;
        Reference reference = referenceService.getRefById(id);
        reference.setName(null);
        reference.setNumber("xxx-qww11223,!");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(reference);
        this.mockMvc.perform(put("/coordinator/references").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("reference-number_Pattern"))
                .andExpect(header().exists("reference-name_NotBlank"))
                .andExpect(header().exists("reference-name_NotNull"));
    }

    @Test
    public void addNewReferenceStatusCreated() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newRef);

        this.mockMvc.perform(post("/coordinator/references").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));
    }

    @Test
    public void addNewReferenceStatusBadEntity() throws Exception {

        newRef.setNumber("1234567890-XXXq!");
        newRef.setHsCode("80908090ds");
        newRef.setSupplierAgreement("55001122335as5");
        newRef.setCustomerAgreement("88008800880s0");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newRef);

        this.mockMvc.perform(post("/coordinator/references").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("reference-hsCode_Pattern"))
                .andExpect(header().exists("reference-supplierAgreement_Pattern"))
                .andExpect(header().exists("reference-number_Pattern"))
                .andExpect(header().exists("reference-customerAgreement_Pattern"));
    }

    @Test
    public void getAllReferencesSortedByNameAsc() throws Exception {
        this.mockMvc.perform(get("/coordinator/references/orderby/name/asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].referenceID").value(2))
                .andExpect(jsonPath("$[1].referenceID").value(3))
                .andExpect(jsonPath("$[2].referenceID").value(1));
    }

    @Test
    public void getAllReferencesSortedByNameDesc() throws Exception {
        this.mockMvc.perform(get("/coordinator/references/orderby/name/desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].referenceID").value(1))
                .andExpect(jsonPath("$[1].referenceID").value(3))
                .andExpect(jsonPath("$[2].referenceID").value(2));
    }

    private String jsonClean(String json) {
        return json.replaceAll("^ +| +$|\\R |, +|\\{ ", "")
                .replace(" : ", ":")
                .replaceAll("   \"| \"", "\"")
                .replaceAll("\\r\\n?|\\n", "")
                .replace(" }", "}");
    }

}
