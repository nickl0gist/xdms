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
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.service.StorageLocationService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 15.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = {"/sql_scripts/referenceTests.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class StorageLocationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageLocationService storageLocationService;

    private StorageLocation storageLocation;

    @Before
    public void init() {
        storageLocation = new StorageLocation();
        storageLocation.setName("Test Name");
        storageLocation.setCode("TestCode");
        storageLocation.setIsActive(true);
    }

    @Test
    public void getAllStorageLocationsTest() throws Exception {
        this.mockMvc.perform(get("/coordinator/stor_loc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void getStorageLocationByStatusOk() throws Exception {
        int id = 3;
        mockMvc.perform(get("/coordinator/stor_loc/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageLocationID").value(3))
                .andExpect(jsonPath("$.code").value("EX25"))
                .andExpect(jsonPath("$.name").value("SL - EX25"));
    }

    @Test
    public void getStorageLocationByIdStatusNOK() throws Exception {
        int id = 30;
        mockMvc.perform(get("coordinator/stor_loc/" + id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void getStorageLocationByIdBadRequest() throws Exception {
        String id = "string";
        this.mockMvc.perform(get("/coordinator/stor_loc/" + id))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void updateStorageLocationTestOk() throws Exception {
        Long id = 4L;
        StorageLocation storageLocation = storageLocationService.getStorageLocationById(id);
        storageLocation.setName("EX-TEST");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(storageLocation);
        this.mockMvc.perform(put("/coordinator/stor_loc").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateStorageLocationTestBadEntity() throws Exception {
        Long id = 3L;
        StorageLocation storageLocation = storageLocationService.getStorageLocationById(id);
        storageLocation.setName("");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(storageLocation);
        this.mockMvc.perform(put("/coordinator/stor_loc").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("storageLocation-name_NotBlank"))
                .andExpect(header().exists("storageLocation-name_Size"));
    }

    @Test
    public void createNewStorageLocationCreated() throws Exception {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(storageLocation);

        mockMvc.perform(post("/coordinator/stor_loc").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(201));
    }

    @Test
    public void createNewStorageLocationStatusBadEntity() throws Exception{

        storageLocation.setName("");
        storageLocation.setCode("");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(storageLocation);

        mockMvc.perform(post("/coordinator/stor_loc").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(422))
                .andExpect(content().string(jsonClean(json)))
                .andExpect(header().exists("storageLocation-name_NotBlank"))
                .andExpect(header().exists("storageLocation-name_Size"))
                .andExpect(header().exists("storageLocation-code_NotBlank"))
                .andExpect(header().exists("storageLocation-code_Size"));
    }

    private String jsonClean(String json) {
        return json.replaceAll("^ +| +$|\\R |, +|\\{ ", "")
                .replace(" : ", ":")
                .replaceAll("   \"| \"", "\"")
                .replaceAll("\\r\\n?|\\n", "")
                .replace(" }", "}");
    }
}
