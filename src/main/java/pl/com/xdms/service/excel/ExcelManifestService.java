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
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.SupplierService;
import pl.com.xdms.service.WarehouseService;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created on 30.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
@Slf4j
public class ExcelManifestService implements ExcelService<Manifest> {

    private final ExcelSupplierService excelSupplierService;
    private final ExcelCustomerService excelCustomerService;
    private final WarehouseService warehouseService;
    private final ExcelProperties excelProperties;
    private final CustomerService customerService;
    private final SupplierService supplierService;


    @Autowired
    public ExcelManifestService(ExcelSupplierService excelSupplierService,
                                WarehouseService warehouseService,
                                ExcelProperties excelProperties,
                                ExcelCustomerService excelCustomerService,
                                CustomerService customerService,
                                SupplierService supplierService) {

        this.excelSupplierService = excelSupplierService;
        this.excelCustomerService = excelCustomerService;
        this.warehouseService = warehouseService;
        this.excelProperties = excelProperties;
        this.customerService = customerService;
        this.supplierService = supplierService;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate() {
        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getPathToManifestUploadTemplate()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet customerSheet = workbook.getSheet(excelProperties.getCustomersSheetName());
            XSSFSheet supplierSheet = workbook.getSheet(excelProperties.getSuppliersSheetName());
            XSSFSheet warehouseSheet = workbook.getSheet(excelProperties.getWarehousesSheetName());

            parseCustomers(customerSheet, workbook);
            parseSuppliers(supplierSheet, workbook);
            parseWarehouses(warehouseSheet, workbook);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.warn("Error occurred while creating file for Manifest Uploading: {}", e.getMessage());
            return null;
        }
    }

    private void parseSuppliers(XSSFSheet supplierSheet, XSSFWorkbook workbook) {
        List<Supplier> supplierList = supplierService.getAllSuppliers();
        int rowIndex = 2;
        for (Supplier supplier : supplierList) {
            Row row = supplierSheet.createRow(rowIndex++);
            excelSupplierService.fillRowWithData(supplier, row, getXssfCellStyle(workbook));
        }
    }

    private void parseCustomers(XSSFSheet customerSheet, XSSFWorkbook workbook) {
        List<Customer> customerList = customerService.getAllCustomers();
        int rowIndex = 2;
        for (Customer customer : customerList) {
            Row row = customerSheet.createRow(rowIndex++);
            excelCustomerService.fillRowWithData(customer, row, getXssfCellStyle(workbook));
        }
    }

    private void parseWarehouses(XSSFSheet warehouseSheet, XSSFWorkbook workbook) {
        List<Warehouse> warehouseList = warehouseService.getAllWarehouses();
        int rowIndex = 2;
        for (Warehouse warehouse : warehouseList) {
            Row row = warehouseSheet.createRow(rowIndex++);
            fillRowWithWarehouses(warehouse, row, getXssfCellStyle(workbook));
        }
    }

    private void fillRowWithWarehouses(Warehouse warehouse, Row row, CellStyle style) {
        Cell whNameCell = row.createCell(0);
        whNameCell.setCellValue(warehouse.getName());

        Cell whCountryCell = row.createCell(1);
        whCountryCell.setCellValue(warehouse.getCountry());

        Cell whCityCell = row.createCell(2);
        whCityCell.setCellValue(warehouse.getCity());

        Cell whStreetCell = row.createCell(3);
        whStreetCell.setCellValue(warehouse.getStreet());

        Cell whEmailCell = row.createCell(4);
        whEmailCell.setCellValue(warehouse.getEmail());

        Cell whTypeCell = row.createCell(5);
        whTypeCell.setCellValue(warehouse.getWhType().getType().toString());

        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            cellIterator.next().setCellStyle(style);
        }
    }

    @Override
    public Map<Long, Manifest> readExcel(File file) {
        return null;
    }

    @Override
    public Map<Long, Manifest> readSheet(Sheet sheet) {
        return null;
    }

    @Override
    public void fillRowWithData(Manifest object, Row row, CellStyle style) {

    }
}
