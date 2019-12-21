package pl.com.xdms.service.excel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.service.CustomerService;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created on 20.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
@Slf4j
public class ExcelCustomerService implements ExcelService<Customer> {

    private ExcelProperties excelProperties;
    private final CustomerService customerService;

    @Autowired
    public ExcelCustomerService(ExcelProperties excelProperties,
                                CustomerService customerService) {
        this.excelProperties = excelProperties;
        this.customerService = customerService;
    }

    @Override
    public Map<Long, Customer> readExcel(File file) {
        log.info("File with Customers received {}", file.getPath());
        Map<Long, Customer> map = readFile(file, excelProperties.getCustomersSheetName());
        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Customers");
        }
        return map;
    }

    @Override
    public Map<Long, Customer> readSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long, Customer> longCustomerHashMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Customer customer = new Customer();
            Row row = rowIterator.next();
            //skip header row
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

            Iterator<Cell> cellIterator = row.cellIterator();
            //Iterate each cell in row
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int cellIndex = cell.getColumnIndex();
                switch (cellIndex) {
                    case 0:
                        customer.setCustomerID(getLongFromCell(cell));
                        break;
                    case 1:
                        customer.setName(getStringFromCell(cell));
                        break;
                    case 2:
                        customer.setCustomerCode(getStringFromNumericCell(cell));
                        break;
                    case 3:
                        customer.setCountry(getStringFromCell(cell));
                        break;
                    case 4:
                        customer.setPostCode(getStringFromCell(cell));
                        break;
                    case 5:
                        customer.setCity(getStringFromCell(cell));
                        break;
                    case 6:
                        customer.setStreet(getStringFromCell(cell));
                        break;
                    case 7:
                        customer.setEmail(getStringFromCell(cell));
                        break;
                    case 8:
                        customer.setIsActive(getBooleanFromCell(cell));
                        break;
                }
                longCustomerHashMap.put(row.getRowNum() + 1L, customer);
            }
            log.info("Customer parsed from Excel file {}", customer);
        }
        return longCustomerHashMap;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate() {

        List<Customer> customerList = customerService.getAllCustomers();

        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getPathToCustomerTemplate()));
             //new FileInputStream(referenceBaseProps.getPathToReferenceTemplate().getFile()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.getSheet(excelProperties.getCustomersSheetName());
            int rowIdx = 2;
            CellStyle style = getXssfCellStyle(workbook);
            for (Customer customer : customerList) {
                Row row = sheet.createRow(rowIdx++);
                fillRowWithData(customer, row, style);

            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn("Error occurred while creating file with Customers from database: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void fillRowWithData(Customer customer, Row row, CellStyle style) {

        Cell idCell = row.createCell(0);
        idCell.setCellValue(customer.getCustomerID());

        Cell nameCell = row.createCell(1);
        nameCell.setCellValue(customer.getName());

        Cell customerCodeCell = row.createCell(2);
        customerCodeCell.setCellValue(customer.getCustomerCode());

        Cell countryCell = row.createCell(3);
        countryCell.setCellValue(customer.getCountry());

        Cell postCodeCell = row.createCell(4);
        postCodeCell.setCellValue(customer.getPostCode());

        Cell cityCell = row.createCell(5);
        cityCell.setCellValue(customer.getCity());

        Cell streetCell = row.createCell(6);
        streetCell.setCellValue(customer.getStreet());

        Cell emailCell = row.createCell(7);
        emailCell.setCellValue(customer.getEmail());

        Cell timeZone = row.createCell(8);
        timeZone.setCellValue(customer.getTimeZone());

        Cell isActiveCell = row.createCell(9);
        isActiveCell.setCellValue(customer.getIsActive());

        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            cellIterator.next().setCellStyle(style);
        }
    }
}
