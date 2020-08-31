package pl.com.xdms.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.WHTypeEnum;
import pl.com.xdms.service.ManifestReferenceService;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 21.07.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class ExcelManifestReferenceService implements ExcelService<ManifestReference> {

    private final ExcelProperties excelProperties;
    private final ManifestReferenceService manifestReferenceService;

    @Autowired
    public ExcelManifestReferenceService(ExcelProperties excelProperties, ManifestReferenceService manifestReferenceService) {
        this.excelProperties = excelProperties;
        this.manifestReferenceService = manifestReferenceService;
    }


    public ByteArrayInputStream instanceToExcelFromTemplate(TruckTimeTable ttt) {
        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getTttDownloadTemplate()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheet(excelProperties.getTttTruckSheetName());
            int rowIdx = 2;
            CellStyle style = getXssfCellStyle(workbook);
            Set<ManifestReference> manifestReferenceSet = ttt.getManifestSet().stream().flatMap(m -> m.getManifestsReferenceSet().stream()).collect(Collectors.toSet());
            for (ManifestReference manifestReference : manifestReferenceSet) {
                Row row = sheet.createRow(rowIdx++);
                fillRowWithData(manifestReference, row, style);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn("Error occurred while creating file with ManifestReferences from database: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void fillRowWithData(ManifestReference manifestReference, Row row, CellStyle style) {
        Cell supplierName = row.createCell(1);
        supplierName.setCellValue(manifestReference.getReference().getSupplier().getName());

        Cell manifestCodeCell = row.createCell(2);
        manifestCodeCell.setCellValue(manifestReference.getManifest().getManifestCode());

        Cell vendorCodeCell = row.createCell(3);
        vendorCodeCell.setCellValue(manifestReference.getReference().getSupplier().getVendorCode());

        Cell receptionCell = row.createCell(4);
        receptionCell.setCellValue(manifestReference.getReceptionNumber());

        Cell deliveryNoteCell = row.createCell(5);
        deliveryNoteCell.setCellValue(manifestReference.getDeliveryNumber());

        Cell cityCell = row.createCell(6);
        TruckTimeTable ttt = manifestReference.getManifest().getTruckTimeTableSet().stream()
                .filter(m -> m.getWarehouse().getWhType().getType().equals(WHTypeEnum.TXD))
                .findFirst().orElse(null);
        cityCell.setCellValue(ttt == null ? "no TXD warehouse" : ttt.getTttArrivalDateReal());

        Cell refNumCell = row.createCell(7);
        refNumCell.setCellValue(manifestReference.getReference().getNumber());

        Cell scheduleAgreementCell = row.createCell(8);
        scheduleAgreementCell.setCellValue(manifestReference.getReference().getSupplierAgreement());

        Cell exCell = row.createCell(9);
        exCell.setCellValue(manifestReference.getReference().getStorageLocation().getCode());

        Cell qtyPlanCell = row.createCell(10);
        qtyPlanCell.setCellValue(manifestReference.getQtyPlanned());

        Cell qtyRealCell = row.createCell(11);
        qtyRealCell.setCellValue(manifestReference.getQtyReal());

        Cell idCell = row.createCell(13);
        idCell.setCellValue(manifestReference.getManifestReferenceId());
    }


    public void saveReceptions(File receptionFile) {
        Map<Long, String> parsedValues = readReceptionExcel(receptionFile);
        List<ManifestReference> manifestReferenceList = manifestReferenceService.getManRefListWithinIdSet(parsedValues.keySet());
        manifestReferenceList.iterator().forEachRemaining(m -> m.setReceptionNumber(parsedValues.get(m.getManifestReferenceId())));
        manifestReferenceService.saveAll(manifestReferenceList);
    }

    private Map<Long, String> readReceptionExcel(File file) {
        log.info("File with Receptions received {}", file.getPath());
        Map<Long, String> map = readReceptionFile(file, excelProperties.getTttTruckSheetName());
        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Receptions");
        }
        return map;
    }

    private Map<Long, String> readReceptionFile(File file, String sheetName){
        try(Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheet(sheetName);
            return readReceptionSheet(sheet);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    private Map<Long, String> readReceptionSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long, String> longManifestReferenceHashMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

            if (row.getCell(4) != null){
                longManifestReferenceHashMap.put(Long.parseLong(getStringFromNumericCell(row.getCell(13))), getStringFromNumericCell(row.getCell(4)));
            }
        }
        return longManifestReferenceHashMap;
    }


    public ByteArrayInputStream instanceToExcelFromTemplate(TPA tpa) {
        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getTpaDownloadTemplate()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheet(excelProperties.getTpaDetailSheetName());
            int rowIdx = 3;

            log.info("TPA {}: {}", tpa.getTpaID(), tpa.getManifestReferenceSet());
            Set<ManifestReference> manifestReferenceSet = tpa.getManifestReferenceSet();
            for (ManifestReference manifestReference : manifestReferenceSet) {
                Row row = sheet.createRow(rowIdx++);
                log.info(manifestReference.getManifestReferenceId().toString());
                fillRowWithDataForPackingList(manifestReference, row);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn("Error occurred while creating file with ManifestReferences from database: {}", e.getMessage());
            return null;
        }
    }

    private void fillRowWithDataForPackingList(ManifestReference manifestReference, Row row) {
        Cell palletQty = row.createCell(1);
        palletQty.setCellValue(manifestReference.getPalletQtyReal());

        Cell palletLength = row.createCell(2);
        palletLength.setCellValue(manifestReference.getReference().getPalletLength());

        Cell palletWidth = row.createCell(3);
        palletWidth.setCellValue(manifestReference.getReference().getPalletWidth());

        Cell palletHeight = row.createCell(4);
        palletHeight.setCellValue(manifestReference.getReference().getPalletHeight());

        Cell boxQty = row.createCell(5);
        boxQty.setCellValue(manifestReference.getBoxQtyReal());

        Cell referenceNum = row.createCell(6);
        referenceNum.setCellValue(manifestReference.getReference().getNumber());

        Cell refQty = row.createCell(7);
        refQty.setCellValue(manifestReference.getQtyReal());

        Cell designationEN = row.createCell(8);
        designationEN.setCellValue(manifestReference.getReference().getDesignationEN());

        Cell designationRU = row.createCell(9);
        designationRU.setCellValue(manifestReference.getReference().getDesignationRU());

        Cell hsCode = row.createCell(10);
        hsCode.setCellValue(manifestReference.getReference().getHsCode());

        Cell unitWeight = row.createCell(11);
        unitWeight.setCellValue(manifestReference.getReference().getWeight());

        Cell totalWeight = row.createCell(12);
        double totalWeightDouble = (double) Math.round(manifestReference.getQtyReal() * manifestReference.getReference().getWeight() * 1000) / 1000;
        totalWeight.setCellValue(totalWeightDouble);

        Cell palletWeight = row.createCell(13);
        palletWeight.setCellValue(manifestReference.getReference().getPalletWeight());

        Cell grossWeight = row.createCell(14);
        grossWeight.setCellValue(manifestReference.getPalletQtyReal() * manifestReference.getReference().getPalletWeight()
                + totalWeightDouble + manifestReference.getReference().getWeightOfPackaging() * manifestReference.getBoxQtyPlanned());

        Cell supplierName = row.createCell(15);
        supplierName.setCellValue(manifestReference.getReference().getSupplier().getName());
    }


    @Override
    public Map<Long, ManifestReference> readSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long, ManifestReference> longManifestReferenceHashMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

            if (row.getCell(4) != null){
                ManifestReference manifestReference = manifestReferenceService.findById(getLongFromCell(row.getCell(13)));
                manifestReference.setReceptionNumber(getStringFromCell(row.getCell(4)));
                longManifestReferenceHashMap.put(manifestReference.getManifestReferenceId(), manifestReference);
            }
        }
        return longManifestReferenceHashMap;
    }

    @Override
    public Map<Long, ManifestReference> readExcel(File file) {
        log.info("File with ManifestReference received {}", file.getPath());
        Map<Long, ManifestReference> map = readFile(file, excelProperties.getTttTruckSheetName());
        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Receptions");
        }
        return map;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate(){
        return null;
    }
}
