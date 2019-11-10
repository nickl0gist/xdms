package pl.com.xdms;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.ExcelService;
import pl.com.xdms.service.ReferenceService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 10.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@SqlGroup({
        @Sql(value = {"/sql_scripts/referenceTests.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ExcelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ReferenceService referenceService;

    /**
     * Tests that The References from Downloaded file in .xlsx are the same as in Database.
     * We get file and uploading back to server and comparing parsed References from it's rows with
     * References which were get from <tt>referenceService</tt>
     * Excel uploading and and downloading are in use.
     * @throws Exception
     */
    @Test
    public void downloadReferenceBase() throws Exception {
        MvcResult result = mockMvc.perform(get("/coordinator/excel/download/references.xlsx").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        File tempFile = File.createTempFile("test", ".xlsx", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(result.getResponse().getContentAsByteArray());
        Map<Long, Reference> testFileMap = excelService.readExcel(tempFile)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(x->x.getKey()-2L, x-> x.getValue()));

        List<Reference> referenceList = referenceService.getAllReferences();
        Map<Long, Reference> test = referenceList.stream().collect(Collectors.toMap(Reference::getReferenceID, x -> x));

        log.info("Result {}",testFileMap.equals(test));
        Assert.assertEquals(testFileMap.get(1L).getReferenceID(),test.get(1L).getReferenceID());
        Assert.assertEquals(testFileMap.get(2L).getName(),test.get(2L).getName());
        Assert.assertEquals(testFileMap.get(3L).getDesignationEN(),test.get(3L).getDesignationEN());

    }

}
