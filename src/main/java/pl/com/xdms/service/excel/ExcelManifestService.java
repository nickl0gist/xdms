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
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.repository.ManifestReferenceRepository;
import pl.com.xdms.service.*;
import pl.com.xdms.service.truck.TruckService;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
    private final TruckService truckService;
    private final WhCustomerService whCustomerService;

    @Autowired
    public ExcelManifestService(ExcelSupplierService excelSupplierService,
                                ExcelCustomerService excelCustomerService,
                                WarehouseService warehouseService,
                                ExcelProperties excelProperties,
                                CustomerService customerService,
                                SupplierService supplierService,
                                ManifestService manifestService,
                                ManifestReferenceRepository manifestReferenceRepository,
                                ReferenceService referenceService,
                                TruckService truckService,
                                WhCustomerService whCustomerService) {
        this.excelSupplierService = excelSupplierService;
        this.excelCustomerService = excelCustomerService;
        this.warehouseService = warehouseService;
        this.excelProperties = excelProperties;
        this.customerService = customerService;
        this.supplierService = supplierService;
        this.manifestService = manifestService;
        this.manifestReferenceRepository = manifestReferenceRepository;
        this.referenceService = referenceService;
        this.truckService = truckService;
        this.whCustomerService = whCustomerService;
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

        Cell timeZone = row.createCell(6);
        timeZone.setCellValue(warehouse.getTimeZone());

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
            log.warn("Error occurred while reading the file with Manifests: {}", e.getMessage());
        }

        if (map.isEmpty()) {
            log.warn("Error occurred while reading the file with Manifests");
        }
        return map;
    }


    private Map<Long, Manifest> readSheets(Sheet manifestSheet, Sheet referenceSheet) {

        Iterator<Row> rowIterator = manifestSheet.rowIterator();
        Map<Long, Manifest> resultMap = new HashMap<>();
        Set<TPA> tpaSet = new LinkedHashSet<>();

        while (rowIterator.hasNext()) {
            Manifest manifest = new Manifest();
            Row row = rowIterator.next();

            //skip two first header rows
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

            //fetching Manifest code
            Cell manifestCell = row.getCell(18);
            manifest.setManifestCode(getStringFromCell(manifestCell));

            //fetching Planned values of Pallet qty, Weight, Ldm
            manifest.setPalletQtyPlanned(getLongFromCell(row.getCell(19)).intValue());
            manifest.setTotalWeightPlanned(getDoubleFromCell(row.getCell(21)));
            manifest.setTotalLdmPlanned(getDoubleFromCell(row.getCell(22)));

            //Fetching customer and Supplier
            Customer customer = customerService.getCustomerByName(getStringFromCell(row.getCell(15)));
            Supplier supplier = supplierService.getSupplierByName(getStringFromCell(row.getCell(0)));
            manifest.setSupplier(supplier);
            manifest.setCustomer(customer);

            //fetching reference forecast for manifest from reference_forecast sheet
            // only if there is the TXD warehouse is written in Column L in Excel
            if (getStringFromCell(row.getCell(11)) != null) {
                manifest.setManifestsReferenceSet(getListOfReferences(manifest, referenceSheet, row));
                int boxQty = manifest.getManifestsReferenceSet()
                        .stream()
                        .mapToInt(ManifestReference::getBoxQtyPlanned).sum();
                manifest.setBoxQtyPlanned(boxQty);
            }

            //fetching TTT forecast for manifest from Manifest sheet
            //TODO
            //manifest.setTruckTimeTableSet(getListOfTTT(manifest, manifestSheet));

            //fetching TPA forecast for Manifest from Manifest sheet
            manifest.setTpaSet(getListOfTPA(manifest, row));
            manifest.setIsActive(true);
            updateTpaSet(tpaSet, manifest);

            resultMap.put(row.getRowNum() + 1L, manifest);
        }
        return resultMap;
    }

    private void updateTpaSet(Set<TPA> tpaSet, Manifest manifest) {
        if(tpaSet.isEmpty()){
            tpaSet.addAll(manifest.getTpaSet());
        }
        Set<TPA> collector = new LinkedHashSet<>();
        for (TPA tpa: tpaSet) {
            for (TPA manifestTpa : manifest.getTpaSet()) {
                if(tpaSet.contains(manifestTpa)){
                    if(tpa.getName().equals(manifestTpa.getName())){
                        tpa.getManifestSet().add(manifest);
                    }
                } else {
                    collector.add(manifestTpa);
                }
            }
        }
        tpaSet.addAll(collector);
    }

    /**
     * @param manifest       - current manifest in iteration from @code readSheets().
     * @param referenceSheet - Excel reference_forecast sheet to parse references for manifest.
     * @param manifestRow    - current Row instance in iteration from @code readSheets().
     * @return Set of parsed ManifestReference entities with TPA connected inside.
     */
    private Set<ManifestReference> getListOfReferences(Manifest manifest, Sheet referenceSheet, Row manifestRow) {
        log.info("Fetching references for manifest {} from received file, Row: {}", manifest.getManifestCode(), manifestRow.getRowNum() + 1);

        Iterator<Row> rowIterator = referenceSheet.rowIterator();
        Set<ManifestReference> manifestReferenceSet = new HashSet<>();

        //fetching TXD departure TPA from Manifest Sheet row "manifestRow" and set it as TPA
        // for each manifestReference in current manifest.
        TPA tpa = collectTPA(manifestRow, 11,15);

        while (rowIterator.hasNext()) {
            ManifestReference manifestReference = new ManifestReference();
            Row row = rowIterator.next();

            //skip first 2 header rows
            if ((row.getRowNum() == 0 || row.getRowNum() == 1)) continue;

            //skip if current row doesn't match by manifest code.
            if (!getStringFromCell(row.getCell(0)).equals(manifest.getManifestCode())) continue;

            //fetching reference from base by Agreement Number given in Excel
            Reference reference = referenceService.getRefByAgreement(getStringFromCell(row.getCell(1)));

            // if reference wasn't found by Agreement create empty Reference
            // and set message into Agreement and skip iteration.
            if (reference == null) {
                reference = new Reference();
                reference.setSupplierAgreement("Unknown Agreement!");
                manifestReference.setReference(reference);
                manifestReferenceSet.add(manifestReference);
                continue;
            }

            //If reference was found, set manifestReference entity using method getManifestReference()
            manifestReference = getManifestReference(reference, row);
            manifestReference.setManifest(manifest);

            //add manifestReference to result map and to set of manifestReferences in TPA
            manifestReferenceSet.add(manifestReference);

        }
        // Each manifestReference gets created tpa as TPA
        tpa.setManifestReferenceSet(manifestReferenceSet);
        log.info("manifestReferenceSet collected: {}", tpa.getManifestReferenceSet());
        manifestReferenceSet.forEach(x -> x.setTpa(tpa));

        return manifestReferenceSet;
    }

    //TODO
    /**
     * Calculates Set of TPAs for the Manifest. Set could contain maximum 2 elements: TPA from CC and TPA from XD
     * Also it could contain no TPA for XD and CC when manifest going directly to TXD from supplier or when it is MilkRun
     * which collects goods from suppliers and drive them to TXD.
     * @param manifest - Manifest entity collected from Excel
     * @param row      - Sheet Row where information about TPAs will be collected.
     * @return List of TPA
     */
    private Set<TPA> getListOfTPA(Manifest manifest, Row row) {
        TPA ccTPA = collectingTpaFromCC(manifest, row);
        TPA xdTPA = collectingTpaFromXD(manifest, row);
        Set<TPA> tpaList = new LinkedHashSet<>();
        tpaList.add(ccTPA);
        tpaList.add(xdTPA);
        return tpaList.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private TPA collectingTpaFromCC(Manifest manifest, Row row) {
        TPA ccTPA = null;
        if (getStringFromCell(row.getCell(3)) != null) {
            if(getStringFromCell(row.getCell(7)) != null){
                ccTPA = collectTPA(row, 3,7);
                ccTPA.getManifestSet().add(manifest);
            } else if (getStringFromCell(row.getCell(11)) != null){
                ccTPA = collectTPA(row, 3,11);
                ccTPA.getManifestSet().add(manifest);
            }
        }
        return ccTPA;
    }

    private TPA collectingTpaFromXD(Manifest manifest, Row row) {
        TPA xdTPA = null;
        if (getStringFromCell(row.getCell(7)) != null) {
            if(getStringFromCell(row.getCell(11)) != null){
                xdTPA = collectTPA(row, 7,11);
                xdTPA.getManifestSet().add(manifest);
            } else if (getStringFromCell(row.getCell(15)) != null){
                xdTPA = collectTPA(row, 7,15);
                xdTPA.getManifestSet().add(manifest);
            }
        }
        return xdTPA;
    }

    /**
     * Calculates appropriate TPA according existed TpaDaysSettings for current pair Warehouse Customer. If the planned dates
     * of arriving to TXD, Departure from it and arriving to Customer are in he Past the TPA will be returned with status Error.
     * @param warehouseNameColumn - index of Column where will try to find Warehouse name
     * @param customerNameColumn - index of Column where will try to find Customer name
     * @param row - current Row instance in iteration from @code readSheets() given through @code getListOfReferences().
     * @return TPA instance fetched from given Row. This TPA will be used for Trade Cross Dock departure plan.
     */
    private TPA collectTPA(Row row, int warehouseNameColumn, int customerNameColumn) {
        TPA tpa = new TPA();
        tpa.setName(getStringFromCell(row.getCell(warehouseNameColumn + 3)));
        log.info("Collecting new TPA with name [{}]", tpa.getName());

        Warehouse warehouse = warehouseService.getWarehouseByName(getStringFromCell(row.getCell(warehouseNameColumn)));

        Customer customer = customerService.getCustomerByName(getStringFromCell(row.getCell(customerNameColumn)));
        WhCustomer whCustomer = whCustomerService.findByWarehouseAndCustomer(warehouse, customer);

        // Creation ZoneDateTime of the moment when Manifest Should arrive to customer
        // based on LocalTime and ZoneId of Customer
        log.info("dateTimeETA = Date {}, Time {}",getLocalDateCell(row.getCell(customerNameColumn + 1)), getLocalTimeCell(row.getCell(customerNameColumn + 2)));
        ZonedDateTime dateTimeETA = ZonedDateTime.of(getLocalDateTime(row.getCell(customerNameColumn + 1),
                row.getCell(customerNameColumn + 2)), ZoneId.of(customer.getTimeZone()));

        // Creation ZoneDateTime of the moment when Manifest Should arrive to warehouse
        // based on LocalTime and ZoneId of Warehouse
        log.info("dateTimeTxdEta = Date {}, Time {}",getLocalDateCell(row.getCell(warehouseNameColumn + 1)), getLocalTimeCell(row.getCell(warehouseNameColumn + 2)));
        ZonedDateTime dateTimeTxdEta = ZonedDateTime.of(getLocalDateTime(row.getCell(warehouseNameColumn + 1),
                row.getCell(warehouseNameColumn + 2)), ZoneId.of(warehouse.getTimeZone()));

        // Creation ZoneDateTime of the moment when Manifest Should be released from Warehouse
        // based on Warehouse_Customer TransitTime and dateTimeETA and timeZone of Warehouse
        ZonedDateTime dateTimeETD = dateTimeETA.minusMinutes(whCustomerService.getTTminutes(whCustomer)).withZoneSameInstant(ZoneId.of(warehouse.getTimeZone()));

        // IF all above calculated dates are in the past return tpa with status Error.
        if (dateTimeETA.isBefore(ZonedDateTime.now())
                || dateTimeTxdEta.isBefore(ZonedDateTime.now())
                || dateTimeETD.isBefore(ZonedDateTime.now())) {

            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("Line 375 Error occurred here");
            return tpa;
        }

        // if amount of days between TPA ETD and ZoneDateTime.now() greater than 180 days, tpa status will
        // be assigned as Error
        if (ChronoUnit.DAYS.between(dateTimeETD, ZonedDateTime.now()) > 180) {
            tpa.setDeparturePlan(dateTimeETD.toLocalDateTime());
            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("Line 384 Error occurred here");
            return tpa;
        }

        //Calculation of appropriate TPA and ZoneTimeDateStamp when the manifest will be sent
        Map.Entry<ZonedDateTime, TpaDaysSetting> calculatedETD = truckService.getAppropriateTpaSetting(dateTimeETD, whCustomer).entrySet().iterator().next();
        TpaDaysSetting appropriateTpa = calculatedETD.getValue();
        ZonedDateTime calculatedDateTimeETD = calculatedETD.getKey();

        //if calculated ETD is before ETA of manifest arriving to warehouse set TPA status Error
        if (calculatedDateTimeETD.isBefore(dateTimeTxdEta)) {
            tpa.setDeparturePlan(calculatedDateTimeETD.toLocalDateTime());
            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("Line 397 Error occurred here");
            return tpa;
        }

        tpa.setDeparturePlan(calculatedDateTimeETD.toLocalDateTime());
        tpa.setTpaDaysSetting(appropriateTpa);
        tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.BUFFER));
        return tpa;
    }


    //TODO
    private Set<TruckTimeTable> getListOfTTT(Manifest manifest, Sheet manifestSheet) {
        Set<TruckTimeTable> tttList = new HashSet<>();
        Iterator<Row> rowIterator = manifestSheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;
            if (manifest.getManifestCode().equals(getStringFromCell(row.getCell(18)))) {
                TruckTimeTable ccTTT = new TruckTimeTable();
                TruckTimeTable xdTTT = new TruckTimeTable();
                TruckTimeTable txdTTT = new TruckTimeTable();

                ccTTT.setTruckName(getStringFromCell(row.getCell(0)) + getDateFromCell(row.getCell(1)));

                log.info("Tuck name {}", ccTTT.getTruckName());
                break;
            }

        }
        return tttList;
    }

    /**
     * @param reference - taken from database and parsed from method @code getListOfReferences
     * @param row       - current row in iteration from method getListOfReferences
     * @return created ManifestReference Instance
     */
    private ManifestReference getManifestReference(Reference reference, Row row) {

        ManifestReference manifestReference = new ManifestReference();
        manifestReference.setReference(reference);

        double qtyPlanned = getDoubleFromCell(row.getCell(3));
        manifestReference.setQtyPlanned(qtyPlanned);

        int qtyOfBoxes = (int) Math.ceil(qtyPlanned / reference.getPcsPerPU());
        manifestReference.setBoxQtyPlanned(qtyOfBoxes);

        int qtyOfPallets = (int) Math.ceil(qtyOfBoxes / (double) reference.getPuPerHU());
        manifestReference.setPalletQtyPlanned(qtyOfPallets);

        double nettWeight = reference.getWeight() * manifestReference.getQtyPlanned();
        manifestReference.setNetWeight(nettWeight);

        double grossWeight = nettWeight + qtyOfBoxes * reference.getWeightOfPackaging();
        manifestReference.setGrossWeightPlanned(grossWeight);

        manifestReference.setStackability(reference.getStackability());
        manifestReference.setPalletWeight(reference.getPalletWeight());
        manifestReference.setPalletWidth(reference.getPalletWidth());
        manifestReference.setPalletLength(reference.getPalletLength());
        manifestReference.setPalletHeight(reference.getPalletHeight());

        return manifestReference;
    }

    //Empty method
    @Override
    public Map<Long, Manifest> readSheet(Sheet sheet) {
        return null;
    }
    //Empty method
    @Override
    public void fillRowWithData(Manifest object, Row row, CellStyle style) {

    }
}
