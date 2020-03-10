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
import pl.com.xdms.service.excel.ExcelManifestService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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

    /**
     * Test the endpoint "/coordinator/excel/download/manifest_upload_template.xlsx" which downloads the template of Excel file
     * to be uploaded as forecast into the system later.
     * @throws Exception
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
            log.warn("Error occurred while creating file one received from endpoint", e.getMessage());

        }
    }

    /**
     * Test for the file manifestUploadTemplateTest1.xlsx which contains 3 manifests to check by the system.
     * All manifests dont have any TPA and TTT, no dates of pickup and no dates of delivery to supplier and any warehouse.
     * Also it has one additional row which doesnt have manifest number and should be ignored by the system and stop
     * searching on this row.
     * According to idea all of the manifests shouldn't be available to be passed to DB and
     *
     * @throws Exception
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
     * Test of uploading forecast within file manifestUploadTemplateTest2.xlsx. It contains 3 manifests to be implemented
     * into the system. The first manifest should pass all validations and ready to be implemented into the system while
     * the two other should have isActive=false.
     * @throws Exception
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
     * @throws Exception
     */
    @Test
    public void uploadFileWithProperInformation() throws Exception{
        updateManifestForecastWithProperInfo();

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
     * @throws Exception
     */
    @Test
    public void uploadFileWithProperInformationAndSaveItToDB() throws Exception{
        updateManifestForecastWithProperInfo();

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
        Assert.assertEquals(3, excelManifestService.getManifestReferenceService().getAllManifestReferences().size());
        Assert.assertEquals(7, excelManifestService.getTruckService().getTpaService().getAllTpa().size());
        Assert.assertEquals(7, excelManifestService.getTruckService().getTttService().getAllTtt().size());

    }

    /**
     * Test case when user tries to upload manifest with the same manifest code which already existing in DB
     * @throws Exception
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
     * @throws Exception
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
     * @throws Exception
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
     * supplier
     * @throws Exception
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
                .andExpect(jsonPath("$[0]['manifestMapDTO']['4'].isActive").value(false));

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
     */
    private void updateManifestForecastWithProperInfo(){
        ClassLoader classLoader = ExcelManifestControllerTest.class.getClassLoader();
        //file with customers to emulate file which will be sent by user.
        File file = new File(classLoader.getResource("excelTests/manifestUploadGoodForecast.xlsx").getFile());

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
            FileOutputStream outputStream = new FileOutputStream("E:/UBU/_XDMS/src/test/resources/excelTests/manifestUploadGoodForecast.xlsx");
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
