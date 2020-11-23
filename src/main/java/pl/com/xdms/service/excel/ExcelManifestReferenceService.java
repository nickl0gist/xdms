package pl.com.xdms.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.WHTypeEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.ManifestReferenceService;
import sun.plugin.dom.exception.WrongDocumentException;

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
@PropertySource("classpath:messages.properties")
public class ExcelManifestReferenceService implements ExcelService<ManifestReference> {

    @Value("${ttt.warehouse.divider}")
    String tttWarehouseDivider;

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
        idCell.setCellValue(manifestReference.getManifestReferenceId() + tttWarehouseDivider + manifestReference.getManifest().getTruckTimeTableSet().stream()
                .filter(t -> t.getWarehouse().getWhType().getType().equals(WHTypeEnum.TXD))
                .findFirst()
                .orElse(new TruckTimeTable())
                .getWarehouse().getWarehouseID());
    }

    public void saveReceptions(File receptionFile, Warehouse warehouse) throws Exception{
        Map<Long, String> parsedValues = readReceptionExcel(receptionFile, warehouse);
        List<ManifestReference> manifestReferenceList = manifestReferenceService.getManRefListWithinIdSet(parsedValues.keySet());
        manifestReferenceList.iterator().forEachRemaining(m -> m.setReceptionNumber(parsedValues.get(m.getManifestReferenceId())));
        manifestReferenceService.saveAll(manifestReferenceList);
    }

    private Map<Long, String> readReceptionExcel(File file, Warehouse warehouse) throws Exception{
        log.info("File with Receptions received {}", file.getPath());
        Map<Long, String> map = readReceptionFile(file, excelProperties.getTttTruckSheetName(), warehouse);
        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Receptions");
        }
        return map;
    }

    private Map<Long, String> readReceptionFile(File file, String sheetName, Warehouse warehouse) throws Exception{
        try(Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheet(sheetName);
            return readReceptionSheet(sheet, warehouse);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    /**
     * Map<Long, String> - Long - id of the ManifestReference, String - SAP number
     * @param sheet - Sheet Entity from given Excel file
     * @param warehouse- Warehouse entity where the reception was done.
     * @return Map<Long, String> - Long - id of the ManifestReference, String - SAP number
     */
    private Map<Long, String> readReceptionSheet(Sheet sheet, Warehouse warehouse) throws Exception {
        try{
            Iterator<Row> rowIterator = sheet.rowIterator();
            Map<Long, String> longManifestReferenceHashMap = new HashMap<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

                if (row.getCell(4) != null){
                    try {
                        String manRefIdAndWhId = getStringFromCell(row.getCell(13));
                        String manRefId = manRefIdAndWhId.substring(0, manRefIdAndWhId.indexOf(tttWarehouseDivider));
                        String whId = manRefIdAndWhId.substring(manRefIdAndWhId.indexOf(tttWarehouseDivider)+1);

                        if (!warehouse.getWarehouseID().equals(Long.parseLong(whId))) continue;
                        String reception = getStringFromNumericCell(row.getCell(4));
                        reception = reception.length() == 0 ? null : reception;
                        longManifestReferenceHashMap.put(Long.parseLong(manRefId), reception);

                    }catch (StringIndexOutOfBoundsException siobe){
                        log.info(String.format("Error occurred on row %d", row.getRowNum()));
                        throw new WrongDocumentException(String.format("Error occurred on row %d", row.getRowNum()));
                    }
                }
            }
            return longManifestReferenceHashMap;
        }catch (NullPointerException npe){
            log.info(String.format("No sheet with name %s", excelProperties.getTttTruckSheetName()));
            throw new WrongDocumentException(String.format("No sheet with name %s", excelProperties.getTttTruckSheetName()));
        }
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
                String code = getStringFromCell(row.getCell(13));
                ManifestReference manifestReference = manifestReferenceService.findById(Long.parseLong(code.substring(0, code.indexOf("|"))));
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
