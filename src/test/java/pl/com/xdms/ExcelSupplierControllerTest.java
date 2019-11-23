package pl.com.xdms;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.junit.Assert;
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
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.SupplierService;
import pl.com.xdms.service.excel.ExcelSupplierService;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 18.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@SqlGroup({
        @Sql(value = {"/sql_scripts/createValuesInDBforTests.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ExcelSupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExcelSupplierService excelSupplierService;

    @Autowired
    private SupplierService supplierService;

    @Test
    public void downloadSupplierBase() throws Exception {
        MvcResult result = mockMvc.perform(get("/coordinator/excel/download/suppliers.xlsx").contentType(MediaType.MULTIPART_FORM_DATA))
                //.andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        File tempFile = File.createTempFile("time_"+System.currentTimeMillis()+"_test", ".xlsx", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(result.getResponse().getContentAsByteArray());

        // Map of entities from the received file
        Map<Long, Supplier> testFileMap = excelSupplierService.readExcel(tempFile)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(x -> x.getKey() - 2L, Map.Entry::getValue));

        //Map of entities from Database
        List<Supplier> referenceList = supplierService.getAllSuppliers();
        Map<Long, Supplier> test = referenceList.stream().collect(Collectors.toMap(Supplier::getSupplierID, x -> x));

        //Check assertions
        Assert.assertEquals(testFileMap.get(1L).getSupplierID(), test.get(1L).getSupplierID());
        Assert.assertEquals(testFileMap.get(2L).getName(), test.get(2L).getName());
        Assert.assertEquals(testFileMap.get(3L).getVendorCode(), test.get(3L).getVendorCode());
        Assert.assertEquals(testFileMap.get(4L).getName(), test.get(4L).getName());
    }

    @Test
    public void parsingUploadedFileWithSuppliers() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();

        //file with suppliers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/suppliers.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file.toPath()));

        //uploading of the file and parsing objects from it.
        MvcResult result = mockMvc.perform(multipart("/coordinator/excel/suppliers/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].isActive").value(true))
                .andExpect(jsonPath("$[2].isActive").value(true))
                .andExpect(jsonPath("$[3].isActive").value(false))
                .andExpect(jsonPath("$[4].isActive").value(true))
                .andExpect(jsonPath("$[5].isActive").value(false))
                .andExpect(jsonPath("$[6].isActive").value(true))
                .andExpect(jsonPath("$[7].isActive").value(true))
                .andExpect(jsonPath("$[8].isActive").value(false))
                .andExpect(jsonPath("$[9].isActive").value(false))
                .andExpect(jsonPath("$[10].isActive").value(false))
                .andExpect(jsonPath("$[11].isActive").value(false))
                .andReturn();

        //JSON Array with objects received after parsing the file.
        JSONArray jsonObjects = JsonPath.read(result.getResponse().getContentAsString(), "$");

        //Save attempt of the received JSON objects to database.
        mockMvc.perform(post("/coordinator/excel/suppliers/save_all")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonObjects.toJSONString()))
                .andExpect(status().isCreated());

        //Check if the objects were saved properly.
        Assert.assertEquals(8, supplierService.getAllSuppliers().size());
    }

}
