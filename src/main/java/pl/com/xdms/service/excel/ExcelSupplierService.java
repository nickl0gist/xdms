package pl.com.xdms.service.excel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.SupplierService;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created on 18.11.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
@Slf4j
public class ExcelSupplierService implements ExcelService<Supplier> {

    public ExcelProperties excelProperties;
    public final SupplierService supplierService;

    @Autowired
    public ExcelSupplierService(ExcelProperties excelProperties,
                                SupplierService supplierService) {
        this.excelProperties = excelProperties;
        this.supplierService = supplierService;
    }

    @Override
    public Map<Long, Supplier> readExcel(File file) {
        log.info("File with Suppliers received {}", file.getPath());
        Map<Long, Supplier> map = readFile(file, excelProperties.getSuppliersSheetName());
        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Suppliers");
        }
        return map;
    }

    @Override
    public Map<Long, Supplier> readSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long, Supplier> supplierHashMapnMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Supplier supplier = new Supplier();
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
                        supplier.setSupplierID(getLongFromCell(cell));
                        break;
                    case 1:
                        supplier.setName(getStringFromCell(cell));
                        break;
                    case 2:
                        supplier.setVendorCode(getStringFromNumericCell(cell));
                        break;
                    case 3:
                        supplier.setCountry(getStringFromCell(cell));
                        break;
                    case 4:
                        supplier.setPostCode(getStringFromCell(cell));
                        break;
                    case 5:
                        supplier.setCity(getStringFromCell(cell));
                        break;
                    case 6:
                        supplier.setStreet(getStringFromCell(cell));
                        break;
                    case 7:
                        supplier.setEmail(getStringFromCell(cell));
                        break;
                    case 8:
                        supplier.setIsActive(getBooleanFromCell(cell));
                        break;
                }
                supplierHashMapnMap.put(row.getRowNum() + 1L, supplier);
            }
            log.info("Supplier parsed {}", supplier);
        }
        return supplierHashMapnMap;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate() {

        List<Supplier> supplierList = supplierService.getAllSuppliers();

        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getPathToSupplierTemplate()));
             //new FileInputStream(referenceBaseProps.getPathToReferenceTemplate().getFile()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheet(excelProperties.getSuppliersSheetName());
            int rowIdx = 2;
            CellStyle style = getXssfCellStyle(workbook);
            for (Supplier supplier : supplierList) {
                Row row = sheet.createRow(rowIdx++);
                fillRowWithData(supplier, row, style);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn("Error occurred while creating file with Suppliers from database: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void fillRowWithData(Supplier supplier, Row row, CellStyle style) {
        Cell idCell = row.createCell(0);
        idCell.setCellValue(supplier.getSupplierID());

        Cell nameCell = row.createCell(1);
        nameCell.setCellValue(supplier.getName());

        Cell vendorCodeCell = row.createCell(2);
        vendorCodeCell.setCellValue(supplier.getVendorCode());

        Cell countryCell = row.createCell(3);
        countryCell.setCellValue(supplier.getCountry());

        Cell postCodeCell = row.createCell(4);
        postCodeCell.setCellValue(supplier.getPostCode());

        Cell cityCell = row.createCell(5);
        cityCell.setCellValue(supplier.getCity());

        Cell streetCell = row.createCell(6);
        streetCell.setCellValue(supplier.getStreet());

        Cell emailCell = row.createCell(7);
        emailCell.setCellValue(supplier.getEmail());

        Cell isActiveCell = row.createCell(8);
        isActiveCell.setCellValue(supplier.getIsActive());

        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            cellIterator.next().setCellStyle(style);
        }
    }
}
