package pl.com.xdms;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.dto.ManifestTpaTttDTO;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.excel.ExcelManifestService;
import pl.com.xdms.service.truck.TruckService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 30.01.2020
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
public class ExcelManifestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExcelProperties excelProperties;
    @Autowired
    private ExcelManifestService excelManifestService;
    @Autowired
    private TruckService truckService;

    /**
     * Test the endpoint "/coordinator/excel/download/manifest_upload_template.xlsx" which downloads the template of Excel file
     * to be uploaded as forecast into the system later.
     * @throws Exception of file opening
     */
    @Test
    public void downloadExcelTemplateForMatrixForecast() throws Exception {
        MvcResult result = mockMvc.perform(get("/coordinator/excel/download/manifest_upload_template.xlsx").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        File tempFile = File.createTempFile("time_" + System.currentTimeMillis() + "_test", ".xlsx", null);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(result.getResponse().getContentAsByteArray());

        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(tempFile))) {
            XSSFSheet firstSheet = workbook.getSheetAt(0);
            XSSFSheet secondSheet = workbook.getSheetAt(1);
            XSSFSheet thirdSheet = workbook.getSheetAt(2);
            XSSFSheet fourthSheet = workbook.getSheetAt(3);
            XSSFSheet fifthSheet = workbook.getSheetAt(4);

            Assert.assertEquals(excelProperties.getManifestsSheetName(), firstSheet.getSheetName());
            Assert.assertEquals(excelProperties.getReferenceForecastSheetName(), secondSheet.getSheetName());
            Assert.assertEquals(excelProperties.getSuppliersSheetName(), thirdSheet.getSheetName());
            Assert.assertEquals(excelProperties.getCustomersSheetName(), fourthSheet.getSheetName());
            Assert.assertEquals(excelProperties.getWarehousesSheetName(), fifthSheet.getSheetName());

        } catch (IOException e) {
            log.warn("Error occurred while creating file one received from endpoint {}", e.getMessage());

        }
    }

    /**
     * Test for the file manifestUploadTemplateTest1.xlsx which contains 3 manifests to check by the system.
     * All manifests dont have any TPA and TTT, no dates of pickup and no dates of delivery to supplier and any warehouse.
     * Also it has one additional row which doesnt have manifest number and should be ignored by the system and stop
     * searching on this row.
     * According to idea all of the manifests shouldn't be available to be passed to DB and
     *
     * @throws Exception of file opening
     */
    @Test
    public void uploadFilePostWithNotFullInformationTest() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestUploadTemplateTest1.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file.toPath()));
        MvcResult result = mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andReturn();
    }

    /**
     * Test of uploading forecast within file manifestUploadTemplateTest2.xlsx. It contains 3 manifests to be saved
     * into the system. The first manifest should pass all validations and ready to be saved into the system while
     * the two other should have isActive=false.
     * @throws Exception of file opening
     */
    @Test
    public void uploadFilePostWithOneOkAndTwoNokTest() throws Exception {

        updateManifestUploadTemplateTest2("excelTests/manifestUploadTemplateTest2.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadTemplateTest2.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        MvcResult result = mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andReturn();
    }

    /**
     * Test the case when all manifest are ok in uploaded file. And all of the manifests should have status isActive=true
     * @throws Exception of file opening
     */
    @Test
    public void uploadFileWithProperInformation() throws Exception{
        updateManifestForecastWithProperInfo("excelTests/manifestUploadGoodForecast.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadGoodForecast.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false)); // false because of [ArgentinaName] - isActive=[false]
    }

    /**
     * SAVE(POST) parsed from Excel JSON "/forecast/save"
     * @throws Exception of file opening
     */
    @Test
    public void uploadFileWithProperInformationAndSaveItToDB() throws Exception{
        updateManifestForecastWithProperInfo("excelTests/manifestUploadGoodForecast.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadGoodForecast.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        MvcResult result = mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false)) //Customer: [ArgentinaName] - isActive=[false]
                .andReturn();

        //JSON Array with objects received after parsing the file.
        JSONArray jsonObjects = JsonPath.read(result.getResponse().getContentAsString(), "$");

        //Save attempt of the received JSON objects to database.
        mockMvc.perform(post("/coordinator/excel/forecast/save")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonObjects.toJSONString()))
                .andExpect(status().isCreated());

        //Check if the manifests were saved properly
        Assert.assertEquals(3, excelManifestService.getManifestService().getAllManifests().size());
        Assert.assertEquals(2, excelManifestService.getManifestReferenceService().getAllManifestReferences().size());
        Assert.assertEquals(7, excelManifestService.getTruckService().getTpaService().getAllTpa().size());
        Assert.assertEquals(7, excelManifestService.getTruckService().getTttService().getAllTtt().size());
        int size = excelManifestService.getManifestService().getWarehouseManifestService().findAll().size();
        Assert.assertEquals(5, size);
    }

    /**
     * Test case when user tries to upload manifest with the same manifest code which already existing in DB
     * @throws Exception of file opening
     */
    @Test
    public void manifestValidationTestTheCodeAlreadyExistingInDB() throws Exception{
        updateManifestUploadTemplateTest2("excelTests/manifestUploadTestAlreadyExistingManifestInDb.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadTestAlreadyExistingManifestInDb.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false)); //Customer: [ArgentinaName] - isActive=[false]
    }

    /**
     * Test cases when manifest has wrong constraints such as: No supplier, wrong supplier, No customer, wrong customer,
     * manifest code consists with not allowed signs (+ = > , etc..)
     * @throws Exception of file opening
     */
    @Test
    public void manifestValidationTestWrongManifestConstraints() throws Exception{
        updateManifestUploadTemplateTestWrongManifestConstraints();

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadTestWrongManifestConstraints.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['6'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['7'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['8'].isActive").value(false));
    }

    /**
     * Test the cases when attempts of creation Manifests with Customer or Suppliers isActive status is False
     * @throws Exception of file opening
     */
    @Test
    public void manifestValidationTestInActiveSupplierOrCustomer() throws Exception{
        updateManifestUploadTemplateTestInActiveSupplierOrCustomer("excelTests/manifestUploadTestInactiveSupplierOrCustomer.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadTestInactiveSupplierOrCustomer.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false));
    }

    /**
     * Test the attempt to upload ManifestReference forecast with not existing Schedule Agreement between Platform and
     * supplier and with reference which has supplier agreement isActive=false
     * @throws Exception of file opening
     */
    @Test
    public void manifestReferenceWithNotExistingReference () throws Exception{
        updateManifestUploadTemplateTestInActiveSupplierOrCustomer("excelTests/manifestUploadTestNotExistingScheduleAgreement.xlsx");
        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadTestNotExistingScheduleAgreement.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false));

    }

    /**
     * Test of the case when Customer and Supplier of the Reference from DB matching or not with Customer and Supplier
     * of the Manifest which being saved in DB.
     * @throws Exception of file opening
     */
    @Test
    public void manifestReferenceSupplierCustomerVersusReferenceCustomerSupplierTest() throws Exception{
        updateManifestReferenceSupplCustCompareToReferenceSupplCust();

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestREferenceSupplCustCompareToRefereneceSupplCust.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(true));
    }

    /**
     * Test the case when all manifest are ok in uploaded file. And all of the manifests should have status isActive=true
     * @throws Exception of file opening
     */
    @Test
    public void manifestHasNotExistingCC_XD_TXD() throws Exception{
        updateManifestForecastWithProperInfo("excelTests/manifestUploadNotExistingCC_XD_TXD.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadNotExistingCC_XD_TXD.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false));
    }

    /**
     * Test case when user tries to save TPA or TTT which already exists in DB. The manifest with such TPA or TTT
     * should have status isActive = false.
     * @throws Exception of file opening
     */
    @Test
    public void alreadyExistingTpaAndTttInDatabase() throws Exception{
        updateManifestForecastWithProperInfo("excelTests/manifestUploadGoodForecastAndExistingAlreadyTPAinDB.xlsx");
        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadGoodForecastAndExistingAlreadyTPAinDB.xlsx").getFile());

        Map<Long, ManifestTpaTttDTO> mapBeforePosting = excelManifestService.readExcel(file2);
        TPA tpaToSave = mapBeforePosting.entrySet().iterator().next().getValue().getTpaSetDTO().stream().filter(tpa -> tpa.getName().equals("SAO1")).findFirst().orElse(null); //row 3
        TruckTimeTable tttToSave = mapBeforePosting.entrySet().iterator().next().getValue().getTttSetDTO().stream().filter(ttt -> ttt.getTruckName().equals("STD2")).findFirst().orElse(null); //row 5
        truckService.getTpaService().save(tpaToSave);
        truckService.getTttService().save(tttToSave);

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        MvcResult result = mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andReturn();
    }

    /**
     * Test cases when there are some TTT or TPA has same name for the different conditions
     * (for the same or different Wh-Customer, etc...)
     * @throws Exception of file opening
     */
    @Test
    public void sameTttAndTpaForTwoDifferentWarehouses() throws Exception{
        updateManifestForecastWithTheSameTttAndTpaForDifferentWarehouses();

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadForecastWithConflictTpaAndTtt.xlsx").getFile());

        //Map<Long, ManifestTpaTttDTO> mapBeforePosting = excelManifestService.readExcel(file2);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        MvcResult result = mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['6'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['7'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['8'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['9'].isActive").value(true))
                .andReturn();
    }

    /**
     * Test cases when provided TPA or TTT has dates in The Past of ETA to any of the Warehouses
     * @throws Exception of file opening
     */
    @Test
    public void uploadFileWithTpaAndTttWithDatesInThePast() throws Exception{
        updateManifestForecastWithTpaAndTttWithDatesInThePast();

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadWithLateDatesForTpaOrTtt.xlsx").getFile()); //B3 should be today()+1
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true)) // - control check
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false)) // works for right time in ETA to CC in Excel file
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andExpect(jsonPath("$[0]['tttSetDTO'][?(@.truckName == \"STD1\" && @.['tttStatus'].tttStatusName == \"PENDING\")]").exists())
                .andExpect(jsonPath("$[0]['tttSetDTO'][?(@.truckName == \"STD2\" && @.['tttStatus'].tttStatusName == \"DELAYED\")]").exists())
                .andExpect(jsonPath("$[0]['tttSetDTO'][?(@.truckName == \"NON01\" && @.['tttStatus'].tttStatusName == \"DELAYED\")]").exists())
                .andExpect(jsonPath("$[0]['tttSetDTO'][?(@.truckName == \"ABSD01\" && @.['tttStatus'].tttStatusName == \"DELAYED\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"GRO\" && @.['status'].statusName == \"BUFFER\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"STD1\" && @.['status'].statusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"STD2\" && @.['status'].statusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"ARG1\" && @.['status'].statusName == \"ERROR\")]").exists());
    }

    /**
     * Test cases when date or time, indicated in Excel forecast file, has wrong data type.
     * @throws Exception for file opening
     */
    @Test
    public void uploadFileNotSupportedDatesAndTimeInTpaAndTttTest() throws Exception{
        updateFileWithNotSupportedDatesAndTimeInTpaAndTtt();

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadNotSupportedDatesAndTimeInTpaAndTtt.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andExpect(jsonPath("$[0]['tttSetDTO'][?(@.truckName == \"STD1\" && @.['tttStatus'].tttStatusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tttSetDTO'][?(@.truckName == \"ABSD01\" && @.['tttStatus'].tttStatusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"GRO1\" && @.['status'].statusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"STD1\" && @.['status'].statusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"STD2\" && @.['status'].statusName == \"ERROR\")]").exists());
    }

    @Test
    public void whCustomerIsNotExistingInDatabase () throws Exception{

        updateManifestForecastWithProperInfo("excelTests/manifestUploadWhCustomerIsNotFound.xlsx");

        ClassLoader classLoader = getClass().getClassLoader();
        File file2 = new File(classLoader.getResource("excelTests/manifestUploadWhCustomerIsNotFound.xlsx").getFile());
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Files.readAllBytes(file2.toPath()));

        mockMvc.perform(multipart("/coordinator/excel/manifests/uploadFile").file(mockMultipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['manifestMapDTO']['3'].isActive").value(true))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false))
                .andExpect(jsonPath("$[0]['manifestMapDTO']['5'].isActive").value(false))
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"BRA1\" && @.['status'].statusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"GRO1\" && @.['status'].statusName == \"ERROR\")]").exists())
                .andExpect(jsonPath("$[0]['tpaSetDTO'][?(@.name == \"ARG1\" && @.['status'].statusName == \"ERROR\")]").exists());
    }

    /**
     * Updates file excelTests/manifestUploadNotSupportedDatesAndTimeInTpaAndTtt.xlsx with actual date conditions for
     * test uploadFileNotSupportedDatesAndTimeInTpaAndTttTest
     */
    private void updateFileWithNotSupportedDatesAndTimeInTpaAndTtt() {
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestUploadNotSupportedDatesAndTimeInTpaAndTtt.xlsx").getFile());

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Sheet referenceSheet = workbook.getSheet(excelProperties.getReferenceForecastSheetName());

            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);

            LocalDate nowDate = LocalDate.now();

            //Block where Excel file is being updated to correspond to actual timetable.
            {
                //The First manifest Supplier -> CC -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                //CC Date and Time
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                //TXD Date and Time
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                //Customer Date and Time
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                //The Second Manifest Supplier -> CC -> XD -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                //CC Date and Time
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("16:30:00"));
                //XD Date and Time Wouldn't be updated in order to keep wrong format of date
                //rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(11)));
                //rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));

                //TXD Date and Time
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                //Customer Date and Time
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                //The Third Manifest Supplier -> CC -> XD -> Customer
                //Supplier Date and Time
                rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));

                //CC Date and Time Wouldn't be updated in order to keep wrong format of date
                //rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                //rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //Customer Date and Time
                rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
            }

            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/excelTests/manifestUploadNotSupportedDatesAndTimeInTpaAndTtt.xlsx");
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Updates file excelTests/manifestUploadWithLateDatesForTpaOrTtt.xlsx with actual date conditions for
     * test uploadFileWithTpaAndTttWithDatesInThePast
     */
    private void updateManifestForecastWithTpaAndTttWithDatesInThePast() {
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestUploadWithLateDatesForTpaOrTtt.xlsx").getFile());

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Sheet referenceSheet = workbook.getSheet(excelProperties.getReferenceForecastSheetName());

            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);

            LocalDate nowDate = LocalDate.now();

            //Block where Excel file is being updated to correspond to actual timetable.
            {
                //The First manifest Supplier -> CC -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                //CC Date and Time
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                //TXD Date and Time
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                //Customer Date and Time
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                //The Second Manifest Supplier -> CC -> XD -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.minusDays(2)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                //CC Date and Time
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.minusDays(1)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("06:30:00"));
                //XD Date and Time
                rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(2)));
                rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                //TXD Date and Time
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(6)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                //Customer Date and Time
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                //The Third Manifest Supplier -> CC -> XD -> Customer
                //Supplier Date and Time
                rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.minusDays(4)));
                rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                //CC Date and Time
                rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.minusDays(2)));
                rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate));
                rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //Customer Date and Time
                rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(6)));
                rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
            }

            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/excelTests/manifestUploadWithLateDatesForTpaOrTtt.xlsx");
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Updates schedule for the manifests in the test sameTttAndTpaForTwoDifferentWarehouses
     */
    private void updateManifestForecastWithTheSameTttAndTpaForDifferentWarehouses() {
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestUploadForecastWithConflictTpaAndTtt.xlsx").getFile());

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Sheet referenceSheet = workbook.getSheet(excelProperties.getReferenceForecastSheetName());

            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);
            Row rowMSheet6 = manifestSheet.getRow(5);
            Row rowMSheet7 = manifestSheet.getRow(6);
            Row rowMSheet8 = manifestSheet.getRow(7);
            Row rowMSheet9 = manifestSheet.getRow(8);

            LocalDate nowDate = LocalDate.now();

            //Block where Excel file is being updated to correspond to actual timetable.
            {
                //The First manifest Supplier -> CC -> TXD -> Customer Should be isActive = True as control Row3
                {
                    //The First manifest Supplier -> CC -> TXD -> Customer Should be isActive = True as control
                    //Supplier Date and Time
                    rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                    rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                    //CC Date and Time
                    rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                    rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                    //TXD Date and Time
                    rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                    rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                    //Customer Date and Time
                    rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                    rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));
                }
                //The Same TTT for different WH Row 5 and Row 4
                {
                    //The Same TTT for different WH Row 5 and Row 4
                    //Row 4 The Second Manifest Supplier -> CC -> XD -> TXD -> Customer
                    //Supplier Date and Time
                    rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(6)));
                    rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                    //CC Date and Time
                    rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("17:30:00"));
                    //XD Date and Time
                    rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                    rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                    //TXD Date and Time
                    rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                    rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                    //Customer Date and Time
                    rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                    rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                    //Row 5 The Third Manifest Supplier -> CC -> XD -> Customer
                    //Supplier Date and Time
                    rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                    rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                    //CC Date and Time
                    rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                    //XD Date and Time
                    rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                    rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                    //Customer Date and Time
                    rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                    rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
                }
                //The Same TPA info (Name and ETA to customer) from different WH to same customer
                // Row 6 and Row 7
                {
                    //Row 6 Supplier -> CC -> XD -> TXD -> Customer
                    //Supplier Date and Time
                    rowMSheet6.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet6.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                    //CC Date and Time
                    rowMSheet6.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet6.getCell(6).setCellValue(DateUtil.convertTime("12:00:00"));
                    //XD Date and Time
                    rowMSheet6.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                    rowMSheet6.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                    //TXD Date and Time
                    rowMSheet6.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                    rowMSheet6.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                    //Customer Date and Time
                    rowMSheet6.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                    rowMSheet6.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                    //Row 7 Supplier -> CC -> XD -> Customer
                    //Supplier Date and Time
                    rowMSheet7.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                    rowMSheet7.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                    //CC Date and Time
                    rowMSheet7.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet7.getCell(6).setCellValue(DateUtil.convertTime("10:00:00"));
                    //XD Date and Time
                    rowMSheet7.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                    rowMSheet7.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                    //Customer Date and Time
                    rowMSheet7.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                    rowMSheet7.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
                }
                //The same TPA from different WH to different Customers (same ETA)
                //Row 8 and Row 9
                {
                    //Row 8 Supplier -> CC -> Customer
                    //Supplier Date and Time
                    rowMSheet8.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet8.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                    //CC Date and Time
                    rowMSheet8.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet8.getCell(6).setCellValue(DateUtil.convertTime("12:00:00"));
                    //XD Date and Time
                    rowMSheet8.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                    rowMSheet8.getCell(10).setCellValue(DateUtil.convertTime("08:40:00"));
                    //TXD Date and Time
                    rowMSheet8.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                    rowMSheet8.getCell(14).setCellValue(DateUtil.convertTime("16:00:00"));
                    //Customer Date and Time
                    rowMSheet8.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                    rowMSheet8.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                    //Row 9
                    //Supplier Date and Time
                    rowMSheet9.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(6)));
                    rowMSheet9.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                    //CC Date and Time
                    rowMSheet9.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet9.getCell(6).setCellValue(DateUtil.convertTime("10:00:00"));
                    //TXD Date and Time
                    rowMSheet9.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                    rowMSheet9.getCell(14).setCellValue(DateUtil.convertTime("16:00:00"));
                    //Customer Date and Time
                    rowMSheet9.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                    rowMSheet9.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
                }
            }

            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/excelTests/manifestUploadForecastWithConflictTpaAndTtt.xlsx");
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Fills information for the file which used for test case in
     * manifestReferenceSupplierCustomerVersusReferenceCustomerSupplierTest
     */
    private void updateManifestReferenceSupplCustCompareToReferenceSupplCust() {
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestREferenceSupplCustCompareToRefereneceSupplCust.xlsx").getFile());

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Sheet referenceSheet = workbook.getSheet(excelProperties.getReferenceForecastSheetName());

            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);

            LocalDate nowDate = LocalDate.now();

            //Block where Excel file is being updated to correspond to actual timetable.
            {
                //The First manifest Supplier -> CC -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                //CC Date and Time
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                //TXD Date and Time
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                //Customer Date and Time
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                //The Second Manifest Supplier -> CC -> XD -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                //CC Date and Time
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("16:30:00"));
                //XD Date and Time
                rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                //TXD Date and Time
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                //Customer Date and Time
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                //The Third Manifest Supplier -> CC -> XD -> Customer
                //Supplier Date and Time
                rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                //CC Date and Time
                rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //TXD Date and Time
                rowMSheet5.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet5.getCell(14).setCellValue(DateUtil.convertTime("09:00:00"));
                //Customer Date and Time
                rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
            }

            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/excelTests/manifestREferenceSupplCustCompareToRefereneceSupplCust.xlsx");
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Fills actual date values in the file manifestUploadTestInactiveSupplierOrCustomer.xlsx to test cases
     * when Supplier or Customer have status isActive = false
     * Updates dates in for test manifestReferenceWithNotExistingReference()
     */
    private void updateManifestUploadTemplateTestInActiveSupplierOrCustomer(String path) {
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource(path).getFile());

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);
            LocalDate nowDate = LocalDate.now();

            //Block where Excel file is being updated to correspond to actual timetable.
            {
                //The First manifest Supplier -> CC -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                //CC Date and Time
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                //TXD Date and Time
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                //Customer Date and Time
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                //The Second Manifest Supplier -> CC -> XD -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                //CC Date and Time
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("12:30:00"));
                //XD Date and Time
                rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                //TXD Date and Time
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                //Customer Date and Time
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                if(rowMSheet5 != null){
                    //The Third Manifest Supplier -> CC -> XD -> TXD -> Customer
                    //Supplier Date and Time
                    rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                    rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                    //CC Date and Time
                    rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                    rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("12:30:00"));
                    //XD Date and Time
                    rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(11)));
                    rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                    //TXD Date and Time
                    rowMSheet5.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                    rowMSheet5.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                    //Customer Date and Time
                    rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                    rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));
                }

            }
            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/" + path);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Fills file manifestUploadTestWrongManifestConstraints.xlsx with actual date values for 6 rows to be tested in
     * manifestValidationTestWrongManifestConstraints
     */
    private void updateManifestUploadTemplateTestWrongManifestConstraints() {
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestUploadTestWrongManifestConstraints.xlsx").getFile());


        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);
            Row rowMSheet6 = manifestSheet.getRow(5);
            Row rowMSheet7 = manifestSheet.getRow(6);
            Row rowMSheet8 = manifestSheet.getRow(7);
            LocalDate nowDate = LocalDate.now();
            {
                //The First manifest Supplier -> CC -> TXD -> Customer (Good manifest for tests)
                //Supplier Date and Time
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                //CC Date and Time
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                //TXD Date and Time
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                //Customer Date and Time
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                //The Second Manifest Supplier -> CC -> XD -> TXD -> Customer (Wrong supplier)
                //Supplier Date and Time
                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                //CC Date and Time
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("16:30:00"));
                //XD Date and Time
                rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("16:40:00"));
                //TXD Date and Time
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                //Customer Date and Time
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                //The Third Manifest Supplier -> CC -> XD -> Customer (Wrong Customer)
                //Supplier Date and Time
                rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                //CC Date and Time
                rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //Customer Date and Time
                rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));

                //The Fourth Manifest Supplier -> CC -> XD -> Customer (No Supplier)
                //Supplier Date and Time
                rowMSheet6.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet6.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                //CC Date and Time
                rowMSheet6.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet6.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet6.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet6.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //Customer Date and Time
                rowMSheet6.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet6.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));

                //The Fifth Manifest Supplier -> CC -> XD -> Customer (No Customer)
                //Supplier Date and Time
                rowMSheet7.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet7.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                //CC Date and Time
                rowMSheet7.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet7.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet7.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet7.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //Customer Date and Time
                rowMSheet7.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet7.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));

                //The sixth Manifest Supplier -> CC -> XD -> Customer (Wrong Manifest Code)
                //Supplier Date and Time
                rowMSheet8.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet8.getCell(2).setCellValue(DateUtil.convertTime("10:40:00"));
                //CC Date and Time
                rowMSheet8.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet8.getCell(6).setCellValue(DateUtil.convertTime("11:30:00"));
                ///TXD Date and Time
                rowMSheet8.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet8.getCell(14).setCellValue(DateUtil.convertTime("11:05:00"));
                //Customer Date and Time
                rowMSheet8.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet8.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
            }
            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/excelTests/manifestUploadTestWrongManifestConstraints.xlsx");
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Method dedicated to fill data in the template with actual dates for anticipated manifest at line 3.
     * The manifest from line 3 should pass validation.
     */
    private void updateManifestUploadTemplateTest2(String path){
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource(path).getFile());


        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            LocalDate nowDate = LocalDate.now();
            {
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(18)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(6)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("9:30:00"));
                rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("16:40:00"));
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(18)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));
            }
            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/" + path);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Method Updates information in the file excelTests/manifestUploadGoodForecast.xlsx with proper information
     * for manifest forecast used to be checked for OK tests and save(POST) using endpoint "coordinator/excel/forecast/save"
     * @param path - path to file to be updated
     */
    private void updateManifestForecastWithProperInfo(String path){
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource(path).getFile());

        try (FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(inputStream)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Sheet referenceSheet = workbook.getSheet(excelProperties.getReferenceForecastSheetName());

            Row rowMSheet3 = manifestSheet.getRow(2);
            Row rowMSheet4 = manifestSheet.getRow(3);
            Row rowMSheet5 = manifestSheet.getRow(4);

            LocalDate nowDate = LocalDate.now();

            //Block where Excel file is being updated to correspond to actual timetable.
            {
                //The First manifest Supplier -> CC -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet3.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(9)));
                rowMSheet3.getCell(2).setCellValue(DateUtil.convertTime("10:30:00"));
                //CC Date and Time
                rowMSheet3.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet3.getCell(6).setCellValue(DateUtil.convertTime("15:30:00"));
                //TXD Date and Time
                rowMSheet3.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(12)));
                rowMSheet3.getCell(14).setCellValue(DateUtil.convertTime("11:00:00"));
                //Customer Date and Time
                rowMSheet3.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(25)));
                rowMSheet3.getCell(18).setCellValue(DateUtil.convertTime("10:00:00"));

                //The Second Manifest Supplier -> CC -> XD -> TXD -> Customer
                //Supplier Date and Time
                rowMSheet4.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(2).setCellValue(DateUtil.convertTime("11:30:00"));
                //CC Date and Time
                rowMSheet4.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(8)));
                rowMSheet4.getCell(6).setCellValue(DateUtil.convertTime("16:30:00"));
                //XD Date and Time
                rowMSheet4.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet4.getCell(10).setCellValue(DateUtil.convertTime("12:40:00"));
                //TXD Date and Time
                rowMSheet4.getCell(13).setCellValue(localDateToDate(nowDate.plusDays(13)));
                rowMSheet4.getCell(14).setCellValue(DateUtil.convertTime("17:00:00"));
                //Customer Date and Time
                rowMSheet4.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(17)));
                rowMSheet4.getCell(18).setCellValue(DateUtil.convertTime("12:00:00"));

                //The Third Manifest Supplier -> CC -> XD -> Customer
                //Supplier Date and Time
                rowMSheet5.getCell(1).setCellValue(localDateToDate(nowDate.plusDays(4)));
                rowMSheet5.getCell(2).setCellValue(DateUtil.convertTime("08:30:00"));
                //CC Date and Time
                rowMSheet5.getCell(5).setCellValue(localDateToDate(nowDate.plusDays(7)));
                rowMSheet5.getCell(6).setCellValue(DateUtil.convertTime("10:30:00"));
                //XD Date and Time
                rowMSheet5.getCell(9).setCellValue(localDateToDate(nowDate.plusDays(10)));
                rowMSheet5.getCell(10).setCellValue(DateUtil.convertTime("13:55:00"));
                //Customer Date and Time
                rowMSheet5.getCell(17).setCellValue(localDateToDate(nowDate.plusDays(11)));
                rowMSheet5.getCell(18).setCellValue(DateUtil.convertTime("17:00:00"));
            }

            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/" + path);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }
    }

    /**
     * Converter of LocalDate to Date.
     * @param localDate to be converted
     * @return Date entity created after conversion
     */
    private static Date localDateToDate(LocalDate localDate) {
//        if(localDate.getDayOfWeek().getValue() == 6 ||  localDate.getDayOfWeek().getValue() == 7){
//            localDate = localDate.plusDays(2);
//        }
        ZoneId defaultZoneId = ZoneId.systemDefault();
        return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    }
}
