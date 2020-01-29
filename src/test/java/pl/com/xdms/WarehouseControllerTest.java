package pl.com.xdms;

/**
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

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
import pl.com.xdms.domain.warehouse.WHType;
import pl.com.xdms.domain.warehouse.WHTypeEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.WhCustomerService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = {"/sql_scripts/createValuesInDBforTests.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class WarehouseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private WhCustomerService whCustomerService;

    private Warehouse newWarehouse;

    @Before
    public void init(){

        WHType newWhType = new WHType();
        newWhType.setType(WHTypeEnum.CC);

        newWarehouse = new Warehouse();
        newWarehouse.setCity("Bouconvillers");
        newWarehouse.setCountry("FR");
        newWarehouse.setIsActive(true);
        newWarehouse.setName("Cassier CC");
        newWarehouse.setPostCode("10-555");
        newWarehouse.setStreet("Renault, 12");
        newWarehouse.setEmail("email@cassier.fr");
        newWarehouse.setWhType(newWhType);
        newWarehouse.setUrlCode("cc_cass");
        newWarehouse.setTimeZone("GMT+02");
    }

    @Test
    public void getAllWarehousesTest() throws Exception {
        mockMvc.perform(get("/admin/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    public void getWarehouseByIdStatusOk() throws Exception {
        mockMvc.perform(get("/admin/warehouses/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Świebodzice"));
    }

    @Test
    public void getWarehouseByIdStatusNotFound() throws Exception {
        mockMvc.perform(get("/admin/warehouses/7"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void getOnlyActiveWarehousesTest() throws Exception {
        mockMvc.perform(get("/admin/warehouses/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void getOnlyNotActiveWarehousesTest() throws Exception {
        mockMvc.perform(get("/admin/warehouses/not_active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void getAllWarehousesOrderedByName() throws Exception {
        mockMvc.perform(get("/admin/warehouses/ordered_by/name/asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].warehouseID").value(3))
                .andExpect(jsonPath("$[4].warehouseID").value(4));
    }

    @Test
    public void getAllWarehousesOrderedByCountry() throws Exception {
        mockMvc.perform(get("/admin/warehouses/ordered_by/country/desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].warehouseID").value(5))
                .andExpect(jsonPath("$[4].warehouseID").value(3));
    }

    @Test
    public void getWarehousesBySearch() throws Exception {
        String search = "PL";
        mockMvc.perform(get("/admin/warehouses/search/" + search))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].warehouseID").value(1))
                .andExpect(jsonPath("$[1].warehouseID").value(2));
    }

    @Test
    public void updateWarehouseTestStatusOk() throws Exception {
        Long id = 1L;
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        warehouse.setIsActive(false);
        warehouse.setName("namename");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(warehouse);
        this.mockMvc.perform(put("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.name").value("namename"));
    }

    @Test
    public void updateWarehouseTestStatusBadEntity() throws Exception {
        Long id = 1L;
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        warehouse.setIsActive(false);
        warehouse.setName("");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(warehouse);
        this.mockMvc.perform(put("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("warehouse-name_Size"))
                .andExpect(header().exists("warehouse-name_NotBlank"));
    }

    @Test
    public void updateWarehouseWhichNotExisted() throws Exception {
        newWarehouse.setWarehouseID(10L);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newWarehouse);
        this.mockMvc.perform(put("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * @throws Exception
     * Test for checking creation of new Warehouse. Uses WhCustomerService to check if the connection
     * of new created warehouse with each customer in DB was proceeded. All the connections should have
     * status <tt>isActive = false</tt>. New warehouse will be persisted in Customer table with the same info.
     * But it shouldn't have connection with itself in table WhCustomer. All warehouses, which are already existed
     * will have this reflection of Warehouse like Customer, will have connection with Customer created from Warehouse.
     */
    @Test
    public void createWarehouseTestStatusCreated() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newWarehouse);
        Warehouse firstWarehouse = warehouseService.getWarehouseById(1L);

        this.mockMvc.perform(post("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));
        Warehouse persistedWarehouse = warehouseService.getWarehouseByUrl(newWarehouse.getUrlCode());
        Assert.assertEquals(6, warehouseService.getAllWarehouses().size());
        Assert.assertEquals(5, whCustomerService.getAllWhCustomersByWarehouseNotActive(persistedWarehouse).size());
        Assert.assertEquals(2, whCustomerService.getAllWhCustomersByWarehouseNotActive(firstWarehouse).size());
    }

    @Test
    public void createWarehouseTestStatusBadEntity() throws Exception {
        newWarehouse.setName(" ");
        newWarehouse.setCountry(null);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newWarehouse);

        this.mockMvc.perform(post("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().exists("warehouse-country_NotBlank"))
                .andExpect(header().exists("warehouse-country_NotNull"))
                .andExpect(header().exists("warehouse-name_Size"))
                .andExpect(header().exists("warehouse-name_NotBlank"));
    }
    
    @Test
    public void createWarehouseTestWithoutWHType() throws Exception{
        newWarehouse.setWhType(null);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newWarehouse);
        this.mockMvc.perform(post("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(header().exists("warehouse-whType_NotNull"));
    }

    @Test
    public void createWarehouseTestWithEmptyWHTypeTest() throws Exception{
        newWarehouse.setWhType(new WHType());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newWarehouse);
        this.mockMvc.perform(post("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));

        Assert.assertEquals(6, warehouseService.getAllWarehouses().size());
    }

    @Test
    public void createWarehouseTestWithNotPersistedWHType() throws Exception{

        WHType whType = new WHType();
        whType.setWhTypeID(10L);
        newWarehouse.setWhType(whType);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(newWarehouse);
        this.mockMvc.perform(post("/admin/warehouses").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));

        Assert.assertEquals(6, warehouseService.getAllWarehouses().size());
    }

    private String jsonClean(String json) {
        return json.replaceAll("^ +| +$|\\R |, +|\\{ ", "")
                .replace(" : ", ":")
                .replaceAll("   \"| \"", "\"")
                .replaceAll("\\r\\n?|\\n", "")
                .replace(" }", "}");
    }

}
