package pl.com.xdms.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.domain.supplier.Supplier;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created on 26.10.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
@Slf4j
public class ExcelReferenceService implements ExcelService<Reference> {

    private final ExcelProperties referenceBaseProps;
    private final ReferenceService referenceService;
    private final StorageLocationService storageLocationService;
    private final CustomerService customerService;
    private final SupplierService supplierService;

    @Autowired
    public ExcelReferenceService(ExcelProperties referenceBaseProps,
                                 ReferenceService referenceService,
                                 StorageLocationService storageLocationService,
                                 CustomerService customerService,
                                 SupplierService supplierService) {

        this.referenceBaseProps = referenceBaseProps;
        this.referenceService = referenceService;
        this.storageLocationService = storageLocationService;
        this.customerService = customerService;
        this.supplierService = supplierService;
    }

    @Override
    public Map<Long, Reference> readExcel(File file) {
        log.warn("File with References received {}", file.getPath());
        Map<Long, Reference> map = readFile(file);
        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with References");
        }
        return map;
    }

    @Override
    public Map<Long, Reference> readSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long, Reference> referenceMap = new HashMap<>();
        //iterate through rows
        while (rowIterator.hasNext()) {
            Reference reference = new Reference();
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
                        reference.setReferenceID(getLongFromCell(cell));
                        break;
                    case 1:
                        reference.setNumber(getStringFromCell(cell));
                        break;
                    case 2:
                        reference.setName(getStringFromCell(cell));
                        break;
                    case 3:
                        reference.setDesignationEN(getStringFromCell(cell));
                        break;
                    case 4:
                        reference.setDesignationRU(getStringFromCell(cell));
                        break;
                    case 5:
                        reference.setHsCode(getStringFromNumericCell(cell));
                        break;
                    case 6:
                        reference.setWeight(getDoubleFromCell(cell));
                        break;
                    case 7:
                        reference.setWeightOfPackaging(getDoubleFromCell(cell));
                        break;
                    case 8:
                        reference.setStackability(getDoubleFromCell(cell).intValue());
                        break;
                    case 9:
                        reference.setPcsPerPU(getDoubleFromCell(cell).intValue());
                        break;
                    case 10:
                        reference.setPcsPerHU(getDoubleFromCell(cell).intValue());
                        break;
                    case 11:
                        reference.setPalletWeight(getDoubleFromCell(cell).intValue());
                        break;
                    case 12:
                        reference.setPalletHeight(getDoubleFromCell(cell).intValue());
                        break;
                    case 13:
                        reference.setPalletLength(getDoubleFromCell(cell).intValue());
                        break;
                    case 14:
                        reference.setPalletWidth(getDoubleFromCell(cell).intValue());
                        break;
                    case 15:
                        Supplier supplier = supplierService.getSupplierByName(getStringFromCell(cell));
                        reference.setSupplier(supplier);
                        break;
                    case 16:
                        reference.setSupplierAgreement(getStringFromNumericCell(cell));
                        break;
                    case 17:
                        Customer customer = customerService.getCustomerByName(getStringFromCell(cell));
                        reference.setCustomer(customer);
                        break;
                    case 18:
                        reference.setCustomerAgreement(getStringFromNumericCell(cell));
                        break;
                    case 19:
                        StorageLocation storageLocation = storageLocationService.getStorageLocationByCode(getStringFromCell(cell));
                        reference.setStorageLocation(storageLocation);
                        break;
                    case 20:
                        reference.setIsActive(getBooleanFromCell(cell));
                        break;
                }
            }
            log.info("Reference : {}", reference);
            referenceMap.put(row.getRowNum() + 1L, reference);
        }
        return referenceMap;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate(List<Reference> referenceList) {

        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(referenceBaseProps.getPathToReferenceTemplate()));
             //new FileInputStream(referenceBaseProps.getPathToReferenceTemplate().getFile()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowIdx = 2;
            CellStyle style = getXssfCellStyle(workbook);
            for (Reference reference : referenceList) {
                Row row = sheet.createRow(rowIdx++);
                fillRowWithData(reference, row, style);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn("Error occurred while creating file with References from database: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void fillRowWithData(Reference reference, Row row, CellStyle style) {
        Cell idCell = row.createCell(0);
        idCell.setCellValue(reference.getReferenceID());

        Cell numberCell = row.createCell(1);
        numberCell.setCellValue(reference.getNumber());

        Cell nameCell = row.createCell(2);
        nameCell.setCellValue(reference.getName());

        Cell desEnCell = row.createCell(3);
        desEnCell.setCellValue(reference.getDesignationEN());

        Cell desRuCell = row.createCell(4);
        desRuCell.setCellValue(reference.getDesignationRU());

        Cell hsCodeCell = row.createCell(5);
        hsCodeCell.setCellValue(reference.getHsCode());

        Cell weightCell = row.createCell(6);
        weightCell.setCellValue(reference.getWeight());

        Cell weightOfPackCell = row.createCell(7);
        weightOfPackCell.setCellValue(reference.getWeightOfPackaging());

        Cell stakabilityCell = row.createCell(8);
        stakabilityCell.setCellValue(reference.getStackability());

        Cell pcsPerPuCell = row.createCell(9);
        pcsPerPuCell.setCellValue(reference.getPcsPerPU());

        Cell pcsPerHuCell = row.createCell(10);
        pcsPerHuCell.setCellValue(reference.getPcsPerHU());

        Cell palletWeightCell = row.createCell(11);
        palletWeightCell.setCellValue(reference.getPalletWeight());

        Cell palletHeightCell = row.createCell(12);
        palletHeightCell.setCellValue(reference.getPalletHeight());

        Cell palletLengthCell = row.createCell(13);
        palletLengthCell.setCellValue(reference.getPalletLength());

        Cell palletWidthCell = row.createCell(14);
        palletWidthCell.setCellValue(reference.getPalletWidth());

        Cell supplierCell = row.createCell(15);
        supplierCell.setCellValue(reference.getSupplier().getName());

        Cell supplierAgrCell = row.createCell(16);
        supplierAgrCell.setCellValue(reference.getSupplierAgreement());

        Cell customerCell = row.createCell(17);
        customerCell.setCellValue(reference.getCustomer().getName());

        Cell customerAgrCell = row.createCell(18);
        customerAgrCell.setCellValue(reference.getCustomerAgreement());

        Cell slCodeCell = row.createCell(19);
        slCodeCell.setCellValue(reference.getStorageLocation().getCode());

        Cell isActiveCell = row.createCell(20);
        isActiveCell.setCellValue(reference.getIsActive());

        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            cellIterator.next().setCellStyle(style);
        }
    }


}
