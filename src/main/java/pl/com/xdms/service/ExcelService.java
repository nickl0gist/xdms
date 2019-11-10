package pl.com.xdms.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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

import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

/**
 * Created on 26.10.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
@Slf4j
public class ExcelService {

    private final ExcelProperties referenceBaseProps;
    private final ReferenceService referenceService;
    private final StorageLocationService storageLocationService;
    private final CustomerService customerService;
    private final SupplierService supplierService;

    @Autowired
    public ExcelService(ExcelProperties referenceBaseProps,
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

    /**
     * @param file - Path to the file .xlsx which was sent by the user. This file should contain references to be
     *                 updated or saved in database
     * @return - List of References
     */
    public Map<Long,Reference> readExcel(File file) {
        log.warn("File received {}", file.getPath());
        try(Workbook workbook = WorkbookFactory.create(file)) {
            //get excel workbook
            Sheet sheet = workbook.getSheetAt(0);
            return readSheet(sheet);
        } catch (IOException e) {
            log.warn("Cannot get Sheet from given file {}", e.toString());
        }
        return new HashMap<>();
    }

    /**
     * @param sheet - instance of Excel Sheet from Workbook which was sent by user.
     * @return List of References initialized from given sheet.
     */
    private Map<Long,Reference> readSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long,Reference> referenceMap = new HashMap<>();
        //iterate through rows
        while (rowIterator.hasNext()) {
            Reference reference = new Reference();
            Row row = rowIterator.next();
            //skip header row
            if (row.getRowNum() == 0 || row.getRowNum() == 1) {
                continue;
            }
            Iterator<Cell> cellIterator = row.cellIterator();
            //Iterate each cell in row
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int cellIndex = cell.getColumnIndex();
                switch (cellIndex) {
                    case 0:
                        Long id = (((Double) getValueFromCell(cell)).longValue() == 0)
                                ? null
                                : ((Double) getValueFromCell(cell)).longValue();
                        reference.setReferenceID(id);
                        break;
                    case 1:
                        String number = (cell.getCellType() == CellType.NUMERIC)
                                ? ((Double) getValueFromCell(cell)).longValue() + ""
                                : cell.getStringCellValue();
                        reference.setNumber(number);
                        break;
                    case 2:
                        reference.setName((String) getValueFromCell(cell));
                        break;
                    case 3:
                        String designationEn = (cell.getCellType() == CellType.BLANK)
                                ? null
                                :(String) getValueFromCell(cell);
                        reference.setDesignationEN(designationEn);
                        break;
                    case 4:
                        String designationRu = (cell.getCellType() == CellType.BLANK)
                                ? null
                                :(String) getValueFromCell(cell);
                        reference.setDesignationRU(designationRu);
                        break;
                    case 5:
                        String hsCoode = (cell.getCellType() == CellType.NUMERIC)
                                ? ((Double) getValueFromCell(cell)).longValue() + ""
                                : cell.getStringCellValue();
                        reference.setHsCode(hsCoode);
                        break;
                    case 6:
                        reference.setWeight((Double) getValueFromCell(cell));
                        break;
                    case 7:
                        reference.setWeightOfPackaging((Double) getValueFromCell(cell));
                        break;
                    case 8:
                        reference.setStackability(((Double) getValueFromCell(cell)).intValue());
                        break;
                    case 9:
                        reference.setPcsPerPU(((Double) getValueFromCell(cell)).intValue());
                        break;
                    case 10:
                        reference.setPcsPerHU(((Double) getValueFromCell(cell)).intValue());
                        break;
                    case 11:
                        reference.setPalletWeight((Double) getValueFromCell(cell));
                        break;
                    case 12:
                        reference.setPalletHeight(((Double) getValueFromCell(cell)).intValue());
                        break;
                    case 13:
                        reference.setPalletLength(((Double) getValueFromCell(cell)).intValue());
                        break;
                    case 14:
                        reference.setPalletWidth(((Double) getValueFromCell(cell)).intValue());
                        break;
                    case 15:
                        Supplier supplier = supplierService.getSupplierByName(cell.getStringCellValue());
                        reference.setSupplier(supplier);
                        break;
                    case 16:
                        String supplierAgreement = (cell.getCellType() == CellType.NUMERIC)
                                ? ((Double) getValueFromCell(cell)).longValue() + ""
                                : cell.getStringCellValue();
                        reference.setSupplierAgreement(supplierAgreement);
                        break;
                    case 17:
                        Customer customer = customerService.getCustomerByName(cell.getStringCellValue());
                        reference.setCustomer(customer);
                        break;
                    case 18:
                        String customerAgreement = (cell.getCellType() == CellType.NUMERIC)
                                ? ((Double) getValueFromCell(cell)).longValue() + ""
                                : cell.getStringCellValue();
                        reference.setCustomerAgreement(customerAgreement);
                        break;
                    case 19:
                        StorageLocation storageLocation = storageLocationService.getStorageLocationByCode(cell.getStringCellValue());
                        reference.setStorageLocation(storageLocation);
                        break;
                    case 20:
                        reference.setIsActive((Boolean) getValueFromCell(cell));
                        break;
                }
            }
            log.info("Reference : {}", reference);
            referenceMap.put(row.getRowNum()+1L,reference);
        }

        return referenceMap;
    }

    /**
     * Utility method to get cell value based on cell type
     * @param cell - Cell Entity.
     * @return
     */
    private Object getValueFromCell(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return 0.0;
            default:
                return 0.0;
        }
    }

    /**
     * Method is used to make user be able to download reference base in .xlsx file.
     * @param references list of references from controller.
     * @return ByteArrayInputStream with template filled by the information from DB.
     * The file will be filled starting from index pointed in *rowIdx*
     */
    public ByteArrayInputStream referencesToExcelFromTemplate(List<Reference> references) {
        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(referenceBaseProps.getPathToReferenceTemplate()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowIdx = 2;
            for (Reference reference : references) {
                Row row = sheet.createRow(rowIdx++);
                fillRowWithData(reference, row, getXssfCellStyle(workbook));
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    private void fillRowWithData(Reference reference, Row row, CellStyle style) {
        Cell idCell = row.createCell(0);
        idCell.setCellValue(reference.getReferenceID());
        idCell.setCellStyle(style);

        Cell numberCell = row.createCell(1);
        numberCell.setCellValue(reference.getNumber());
        numberCell.setCellStyle(style);

        Cell nameCell = row.createCell(2);
        nameCell.setCellValue(reference.getName());
        nameCell.setCellStyle(style);

        Cell desEnCell = row.createCell(3);
        desEnCell.setCellValue(reference.getDesignationEN());
        desEnCell.setCellStyle(style);

        Cell desRuCell = row.createCell(4);
        desRuCell.setCellValue(reference.getDesignationRU());
        desRuCell.setCellStyle(style);

        Cell hsCodeCell = row.createCell(5);
        hsCodeCell.setCellValue(reference.getHsCode());
        hsCodeCell.setCellStyle(style);

        Cell weightCell = row.createCell(6);
        weightCell.setCellValue(reference.getWeight());
        weightCell.setCellStyle(style);

        Cell weightOfPackCell = row.createCell(7);
        weightOfPackCell.setCellValue(reference.getWeightOfPackaging());
        weightOfPackCell.setCellStyle(style);

        Cell stakabilityCell = row.createCell(8);
        stakabilityCell.setCellValue(reference.getStackability());
        stakabilityCell.setCellStyle(style);

        Cell pcsPerPuCell = row.createCell(9);
        pcsPerPuCell.setCellValue(reference.getPcsPerPU());
        pcsPerPuCell.setCellStyle(style);

        Cell pcsPerHuCell = row.createCell(10);
        pcsPerHuCell.setCellValue(reference.getPcsPerHU());
        pcsPerHuCell.setCellStyle(style);

        Cell palletWeightCell = row.createCell(11);
        palletWeightCell.setCellValue(reference.getPalletWeight());
        palletWeightCell.setCellStyle(style);

        Cell palletHeightCell = row.createCell(12);
        palletHeightCell.setCellValue(reference.getPalletHeight());
        palletHeightCell.setCellStyle(style);

        Cell palletLengthCell = row.createCell(13);
        palletLengthCell.setCellValue(reference.getPalletLength());
        palletLengthCell.setCellStyle(style);

        Cell palletWidthCell = row.createCell(14);
        palletWidthCell.setCellValue(reference.getPalletWidth());
        palletWidthCell.setCellStyle(style);

        Cell supplierCell = row.createCell(15);
        supplierCell.setCellValue(reference.getSupplier().getName());
        supplierCell.setCellStyle(style);

        Cell supplierAgrCell = row.createCell(16);
        supplierAgrCell.setCellValue(reference.getSupplierAgreement());
        supplierAgrCell.setCellStyle(style);

        Cell customerCell = row.createCell(17);
        customerCell.setCellValue(reference.getCustomer().getName());
        customerCell.setCellStyle(style);

        Cell customerAgrCell = row.createCell(18);
        customerAgrCell.setCellValue(reference.getCustomerAgreement());
        customerAgrCell.setCellStyle(style);

        Cell slCodeCell = row.createCell(19);
        slCodeCell.setCellValue(reference.getStorageLocation().getCode());
        slCodeCell.setCellStyle(style);

        Cell isActiveCell = row.createCell(20);
        isActiveCell.setCellValue(reference.getIsActive());
        isActiveCell.setCellStyle(style);
    }

    private XSSFCellStyle getXssfCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(false);
        cellStyle.setBorderBottom(THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK1.getIndex());
        cellStyle.setBorderLeft(THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderRight(THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK1.getIndex());
        return cellStyle;
    }
}
