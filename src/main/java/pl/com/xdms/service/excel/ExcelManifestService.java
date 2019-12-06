package pl.com.xdms.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.repository.ManifestReferenceRepository;
import pl.com.xdms.service.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 30.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class ExcelManifestService implements ExcelService<Manifest> {

    private final ExcelSupplierService excelSupplierService;
    private final ExcelCustomerService excelCustomerService;
    private final WarehouseService warehouseService;
    private final ExcelProperties excelProperties;
    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final ManifestService manifestService;
    private final ManifestReferenceRepository manifestReferenceRepository;
    private final ReferenceService referenceService;

    @Autowired
    public ExcelManifestService(ExcelSupplierService excelSupplierService,
                                ExcelCustomerService excelCustomerService,
                                WarehouseService warehouseService,
                                ExcelProperties excelProperties,
                                CustomerService customerService,
                                SupplierService supplierService,
                                ManifestService manifestService,
                                ManifestReferenceRepository manifestReferenceRepository,
                                ReferenceService referenceService) {
        this.excelSupplierService = excelSupplierService;
        this.excelCustomerService = excelCustomerService;
        this.warehouseService = warehouseService;
        this.excelProperties = excelProperties;
        this.customerService = customerService;
        this.supplierService = supplierService;
        this.manifestService = manifestService;
        this.manifestReferenceRepository = manifestReferenceRepository;
        this.referenceService = referenceService;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate() {
        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getPathToManifestUploadTemplate()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet customerSheet = workbook.getSheet(excelProperties.getCustomersSheetName());
            XSSFSheet supplierSheet = workbook.getSheet(excelProperties.getSuppliersSheetName());
            XSSFSheet warehouseSheet = workbook.getSheet(excelProperties.getWarehousesSheetName());

            insertCustomersToSheet(customerSheet, workbook);
            insertSuppliersToSheet(supplierSheet, workbook);
            insertWarehousesToSheet(warehouseSheet, workbook);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.warn("Error occurred while creating file for Manifest Uploading: {}", e.getMessage());
            return null;
        }
    }

    private void insertSuppliersToSheet(XSSFSheet supplierSheet, XSSFWorkbook workbook) {
        List<Supplier> supplierList = supplierService.getAllSuppliers();
        int rowIndex = 2;
        for (Supplier supplier : supplierList) {
            Row row = supplierSheet.createRow(rowIndex++);
            excelSupplierService.fillRowWithData(supplier, row, getXssfCellStyle(workbook));
        }
    }

    private void insertCustomersToSheet(XSSFSheet customerSheet, XSSFWorkbook workbook) {
        List<Customer> customerList = customerService.getAllCustomers();
        int rowIndex = 2;
        for (Customer customer : customerList) {
            Row row = customerSheet.createRow(rowIndex++);
            excelCustomerService.fillRowWithData(customer, row, getXssfCellStyle(workbook));
        }
    }

    private void insertWarehousesToSheet(XSSFSheet warehouseSheet, XSSFWorkbook workbook) {
        List<Warehouse> warehouseList = warehouseService.getAllWarehouses();
        int rowIndex = 2;
        for (Warehouse warehouse : warehouseList) {
            Row row = warehouseSheet.createRow(rowIndex++);
            fillRowWithWarehouseInfo(warehouse, row, getXssfCellStyle(workbook));
        }
    }

    private void fillRowWithWarehouseInfo(Warehouse warehouse, Row row, CellStyle style) {
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

    /**
     * @param file - Path to the file .xlsx which was sent by the user. This file should contain forecast
     *             manifest sheet and reference sheet to be saved in database.
     * @return map Long value as a key, Map as value. Key Long represents number of row in manifest Sheet.
     * Value Map = Key as Manifest, Value as ManifestReference Array which represents all references related to manifest.
     */
    @Override
    public Map<Long, Manifest> readExcel(File file) {
        log.info("File with Manifests and References received {}", file.getPath());
        Map<Long, Manifest> map = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            //get excel workbook
            Sheet manifestSheet = workbook.getSheet(excelProperties.getManifestsSheetName());
            Sheet referenceSheet = workbook.getSheet(excelProperties.getReferenceForecastSheetName());
            map = readSheets(manifestSheet, referenceSheet);

        } catch (IOException e) {
            log.warn("Error occurred while reading the file with Manifest: {}", e.getMessage());
        }

        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Manifest");
        }
        return map;
    }


    private Map<Long, Manifest> readSheets(Sheet manifestSheet, Sheet referenceSheet) {

        Iterator<Row> rowIterator = manifestSheet.rowIterator();
        Map<Long, Manifest> resultMap = new HashMap<>();

        while (rowIterator.hasNext()) {
            Manifest manifest = new Manifest();
            Row row = rowIterator.next();
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

            Cell manifestCell = row.getCell(18);
            manifest.setManifestCode(getStringFromCell(manifestCell));

            manifest.setPalletQtyPlanned(getLongFromCell(row.getCell(19)).intValue());
            manifest.setTotalWeightPlanned(getDoubleFromCell(row.getCell(21)));
            manifest.setTotalLdmPlanned(getDoubleFromCell(row.getCell(22)));

            Customer customer = customerService.getCustomerByName(getStringFromCell(row.getCell(15)));
            Supplier supplier = supplierService.getSupplierByName(getStringFromCell(row.getCell(0)));

            manifest.setSupplier(supplier);
            manifest.setCustomer(customer);


            //fetching reference forecast for manifest from reference_forecast sheet
            manifest.setManifestsReferenceSet(getListOfReferences(manifest, referenceSheet));

            int boxQty = manifest.getManifestsReferenceSet()
                    .stream()
                    .map(ManifestReference::getBoxQtyPlanned)
                    .collect(Collectors.summingInt(Integer::intValue));
            manifest.setBoxQtyPlanned(boxQty);

            //fetching TTT forecast for manifest from Manifest sheet
            manifest.setTruckTimeTables(getListOfTTT(manifest, manifestSheet));

            manifest.setIsActive(true);

            resultMap.put(row.getRowNum() + 1L, manifest);
        }

        return resultMap;
    }

    //TODO
    private List<TruckTimeTable> getListOfTTT(Manifest manifest, Sheet manifestSheet) {
        return null;
    }

    private Set<ManifestReference> getListOfReferences(Manifest manifest, Sheet referenceSheet) {
        log.info("Fetching references for manifest {} from received file", manifest.getManifestCode());
        Iterator<Row> rowIterator = referenceSheet.rowIterator();
        Set<ManifestReference> manifestReferenceSet = new HashSet<>();

        while (rowIterator.hasNext()) {
            ManifestReference manifestReference = new ManifestReference();
            Row row = rowIterator.next();

            if ((row.getRowNum() == 0 || row.getRowNum() == 1)) continue;
            log.info("Number {}", manifest.getManifestCode());
            log.info("Excel {}", getStringFromCell(row.getCell(0)));
            if (!getStringFromCell(row.getCell(0)).equals(manifest.getManifestCode())) continue;

            Reference reference = referenceService.getRefByAgreement(getStringFromCell(row.getCell(1)));

            if (reference == null) {
                reference = new Reference();
                reference.setSupplierAgreement("Unknown Agreement!");
                manifestReference.setReference(reference);
                manifestReferenceSet.add(manifestReference);
                continue;
            }
            manifestReference = getManifestReference(reference, row);
            manifestReference.setManifest(manifest);
            manifestReferenceSet.add(manifestReference);
        }
        return manifestReferenceSet;
    }

    private ManifestReference getManifestReference(Reference reference, Row row) {

        ManifestReference manifestReference = new ManifestReference();

        manifestReference.setReference(reference);
        manifestReference.setQtyPlanned(getDoubleFromCell(row.getCell(4)));

        int qtyOfBoxes = getLongFromCell(row.getCell(3)).intValue();
        manifestReference.setBoxQtyPlanned(qtyOfBoxes);

        int qtyOfPallets = (int) Math.ceil(qtyOfBoxes / (double) reference.getPuPerHU());
        manifestReference.setPalletQtyPlanned(qtyOfPallets);

        double nettWeight = reference.getWeight() * manifestReference.getQtyPlanned();
        double grossWeight = nettWeight + qtyOfBoxes * reference.getWeightOfPackaging();
        manifestReference.setNetWeight(nettWeight);
        manifestReference.setGrossWeightPlanned(grossWeight);

        manifestReference.setStackability(reference.getStackability());
        manifestReference.setPalletWeight(reference.getPalletWeight());
        manifestReference.setPalletWidth(reference.getPalletWidth());
        manifestReference.setPalletLength(reference.getPalletLength());
        manifestReference.setPalletHeight(reference.getPalletHeight());

        return manifestReference;
    }

    @Override
    public Map<Long, Manifest> readSheet(Sheet sheet) {
        return null;
    }

    @Override
    public void fillRowWithData(Manifest object, Row row, CellStyle style) {

    }
}
