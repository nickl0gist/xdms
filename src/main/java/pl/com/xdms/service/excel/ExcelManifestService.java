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
import pl.com.xdms.domain.dto.ManifestTpaTttDTO;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.*;
import pl.com.xdms.service.truck.TruckService;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 30.11.2019
 *
 * @author Mykola Horkove
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
@Data
public class ExcelManifestService implements ExcelService<ManifestTpaTttDTO> {

    private final ExcelSupplierService excelSupplierService;
    private final ExcelCustomerService excelCustomerService;
    private final WarehouseService warehouseService;
    private final ExcelProperties excelProperties;
    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final ManifestService manifestService;
    private final ManifestReferenceService manifestReferenceService;
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
                                ManifestReferenceService manifestReferenceService,
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
        this.manifestReferenceService = manifestReferenceService;
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
        whNameCell.setCellValue(warehouse.getName().trim());

        Cell whCountryCell = row.createCell(1);
        whCountryCell.setCellValue(warehouse.getCountry().trim());

        Cell whCityCell = row.createCell(2);
        whCityCell.setCellValue(warehouse.getCity().trim());

        Cell whStreetCell = row.createCell(3);
        whStreetCell.setCellValue(warehouse.getStreet().trim());

        Cell whEmailCell = row.createCell(4);
        whEmailCell.setCellValue(warehouse.getEmail().trim());

        Cell whTypeCell = row.createCell(5);
        whTypeCell.setCellValue(warehouse.getWhType().getType().toString().trim());

        Cell timeZone = row.createCell(6);
        timeZone.setCellValue(warehouse.getTimeZone().trim());

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
    public Map<Long, ManifestTpaTttDTO> readExcel(File file) {
        log.info("File with Manifests and References received {}", file.getPath());
        Map<Long, ManifestTpaTttDTO> map = new HashMap<>();
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

    private Map<Long, ManifestTpaTttDTO> readSheets(Sheet manifestSheet, Sheet referenceSheet) {

        Iterator<Row> rowIterator = manifestSheet.rowIterator();
        Map<Long, ManifestTpaTttDTO> resultMap = new HashMap<>();
        ManifestTpaTttDTO collector = new ManifestTpaTttDTO();

        while (rowIterator.hasNext()) {
            Manifest manifest = new Manifest();
            Row row = rowIterator.next();

            //skip two first header rows
            if (row.getRowNum() == 0 || row.getRowNum() == 1) continue;

            //fetching Manifest code
            int manifestNameColumnIndex = 19;
            // if cell with manifest number is null or empty break searching the file
            if(row.getCell(manifestNameColumnIndex) == null || row.getCell(manifestNameColumnIndex).getCellType() == CellType.BLANK) break;

            Cell manifestCell = row.getCell(manifestNameColumnIndex);
            String manifestCode = getStringFromCell(manifestCell);

            manifest.setManifestCode(manifestCode);

            //fetching Planned values of Pallet qty, Weight, Ldm
            manifest.setPalletQtyPlanned(getLongFromCell(row.getCell(manifestNameColumnIndex + 1)).intValue());
            manifest.setTotalWeightPlanned(getDoubleFromCell(row.getCell(manifestNameColumnIndex + 3)));
            manifest.setTotalLdmPlanned(getDoubleFromCell(row.getCell(manifestNameColumnIndex + 4)));

            //Fetching customer and Supplier
            Customer customer = customerService.getCustomerByName(getStringFromCell(row.getCell(manifestNameColumnIndex - 3)));
            Supplier supplier = supplierService.getSupplierByName(getStringFromCell(row.getCell(0)));
            manifest.setSupplier(supplier);
            manifest.setCustomer(customer);

            //fetching reference forecast for manifest from reference_forecast sheet
            // only if there is the TXD warehouse is written in Column M in Excel
            if (getStringFromCell(row.getCell(manifestNameColumnIndex - 7)) != null) {
                Map.Entry<TPA, Set<ManifestReference>> tpaAndManifestReferenceMap = getSetManifestReference(manifest, referenceSheet, row).entrySet().iterator().next();
                collector.getTpaSetDTO().add(tpaAndManifestReferenceMap.getKey());
                collector.getManifestReferenceSetDTO().addAll(tpaAndManifestReferenceMap.getValue());
                int boxQty = collector.getManifestReferenceSetDTO()
                        .stream()
                        .mapToInt(ManifestReference::getBoxQtyPlanned).sum();
                manifest.setBoxQtyPlanned(boxQty);
            }

            //fetching TTT forecast for manifest from Manifest sheet
            manifest.setTruckTimeTableSet(getListOfTTT(row));
            //fetching TPA forecast for Manifest from Manifest sheet
            manifest.setTpaSet(getListOfTPA(row));

            manifest.setIsActive(true);
            collector.getTpaSetDTO().addAll(manifest.getTpaSet());
            collector.getTttSetDTO().addAll(manifest.getTruckTimeTableSet().stream().filter(Objects::nonNull).collect(Collectors.toSet()));
            collector.getManifestMapDTO().put(row.getRowNum() + 1L, manifest);

        }
        collector.getManifestReferenceSetDTO().forEach(x -> collector.getTpaSetDTO().add(x.getTpa()));
        resultMap.put(1L, collector);
        return resultMap;
    }

    /**
     * @param manifest       - current manifest in iteration from @code readSheets().
     * @param referenceSheet - Excel reference_forecast sheet to parse references for manifest.
     * @param manifestRow    - current Row instance in iteration from @code readSheets().
     * @return Set of parsed ManifestReference entities with TPA connected inside.
     */
    private Map<TPA, Set<ManifestReference>> getSetManifestReference(Manifest manifest, Sheet referenceSheet, Row manifestRow) {
        log.info("Fetching references for manifest {} from received file, Row: {}", manifest.getManifestCode(), manifestRow.getRowNum() + 1);

        Iterator<Row> rowIterator = referenceSheet.rowIterator();
        Set<ManifestReference> manifestReferenceSet = new HashSet<>();
        int manifestNameColumnIndex = 19;
        //fetching TXD departure TPA from Manifest Sheet row "manifestRow" and set it as TPA
        // for each manifestReference in current manifest.
        TPA tpa = collectTPA(manifestRow, manifestNameColumnIndex - 7, manifestNameColumnIndex - 3);

        while (rowIterator.hasNext()) {
            ManifestReference manifestReference = new ManifestReference();
            Row row = rowIterator.next();
            log.info("searching in row {} for manifest {}", row.getRowNum() + 1, manifest.getManifestCode());
            //skip first 2 header rows
            if ((row.getRowNum() == 0 || row.getRowNum() == 1)) continue;

            //skip if current row doesn't match by manifest code.
            if (!getStringFromCell(row.getCell(0)).equals(manifest.getManifestCode())) continue;

            //fetching reference from base by Agreement Number given in Excel
            Reference reference = referenceService.getRefByAgreement(getStringFromCell(row.getCell(1)));

            // if reference wasn't found by Agreement create empty Reference
            // and set message into Agreement and skip iteration.
            if (reference == null) {
                log.info("Agreement {} wasn't found", getStringFromCell(row.getCell(1)));
                reference = new Reference();
                reference.setSupplierAgreement("Unknown Agreement!");
                manifestReference.setReference(reference);
                manifestReferenceSet.add(manifestReference);
                manifestReference.setManifestCode(manifest.getManifestCode());
                continue;
            }

            //If reference was found, set manifestReference entity using method getManifestReference()
            manifestReference = getManifestReference(reference, row);
            manifestReference.setManifestCode(manifest.getManifestCode());

            //add manifestReference to result map and to set of manifestReferences in TPA
            manifestReferenceSet.add(manifestReference);
        }
        // Each manifestReference gets created tpa as TPA
        log.info("manifestReferenceSet collected: {}", manifestReferenceSet);
        manifestReferenceSet.forEach(x -> x.setTpaCode(tpa.getName()));
        Map<TPA, Set<ManifestReference>> resultMap = new HashMap<>();
        resultMap.put(tpa, manifestReferenceSet);
        return resultMap;
    }

    /**
     * Calculates Set of TPAs for the Manifest. Set could contain maximum 2 elements: TPA from CC and TPA from XD
     * Also it could contain no TPA for XD and CC when manifest going directly to TXD from supplier or when it is MilkRun
     * which collects goods from suppliers and drive them to TXD.
     *
     * @param row - Sheet Row where information about TPAs will be collected.
     * @return - List of TPA
     */
    private Set<TPA> getListOfTPA(Row row) {
        TPA ccTPA = collectingTpaFromCC(row);
        TPA xdTPA = collectingTpaFromXD(row);
        Set<TPA> tpaList = new LinkedHashSet<>();
        tpaList.add(ccTPA);
        tpaList.add(xdTPA);
        return tpaList.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private TPA collectingTpaFromCC(Row row) {
        TPA ccTPA = null;
        if (getStringFromCell(row.getCell(4)) != null) {
            if (getStringFromCell(row.getCell(8)) != null) {
                ccTPA = collectTPA(row, 4, 8);
            } else if (getStringFromCell(row.getCell(12)) != null) {
                ccTPA = collectTPA(row, 4, 12);
            }
        }
        return ccTPA;
    }

    private TPA collectingTpaFromXD(Row row) {
        TPA xdTPA = null;
        if (getStringFromCell(row.getCell(8)) != null) {
            if (getStringFromCell(row.getCell(12)) != null) {
                xdTPA = collectTPA(row, 8, 12);
            } else if (getStringFromCell(row.getCell(16)) != null) {
                xdTPA = collectTPA(row, 8, 16);
            }
        }
        return xdTPA;
    }

    /**
     * Calculates appropriate TPA according existed TpaDaysSettings for current pair Warehouse Customer. If the planned dates
     * of arriving to TXD, Departure from it and arriving to Customer are in he Past the TPA will be returned with status Error.
     *
     * @param warehouseNameColumn - index of Column where will try to find Warehouse name
     * @param customerNameColumn  - index of Column where will try to find Customer name
     * @param row                 - current Row instance in iteration from @code readSheets() given through @code getSetManifestReference().
     * @return TPA instance fetched from given Row. This TPA will be used for Trade Cross Dock departure plan.
     */
    private TPA collectTPA(Row row, int warehouseNameColumn, int customerNameColumn) {
        TPA tpa = new TPA();
        tpa.setName(getStringFromCell(row.getCell(warehouseNameColumn + 3)));
        log.info("Collecting new TPA with name [{}] ----------------------------", tpa.getName());

        Warehouse warehouse = warehouseService.getWarehouseByName(getStringFromCell(row.getCell(warehouseNameColumn)));

        Customer customer = customerService.getCustomerByName(getStringFromCell(row.getCell(customerNameColumn)));
        if(customer == null){
            log.info("The customer was not found, please check your Excel on row {}", row.getRowNum());
            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            tpa.setDeparturePlan(LocalDateTime.now().toString());
            return tpa;
        }
        WhCustomer whCustomer = whCustomerService.findByWarehouseAndCustomer(warehouse, customer);

        LocalDate dateOfArrivingToCustomer = getLocalDateTime(row.getCell(customerNameColumn + 1), row.getCell(customerNameColumn + 2)).toLocalDate();// getLocalDateCell(row.getCell(customerNameColumn + 1));
        LocalTime timeOfArrivingToCustomer = getLocalDateTime(row.getCell(customerNameColumn + 1), row.getCell(customerNameColumn + 2)).toLocalTime();//getLocalTimeCell(row.getCell(customerNameColumn + 2));
        LocalDate dateOfArrivingToWarehouse = getLocalDateTime(row.getCell(warehouseNameColumn + 1),row.getCell(warehouseNameColumn + 2)).toLocalDate(); //getLocalDateCell(row.getCell(warehouseNameColumn + 1));
        LocalTime timeOfArrivingToWarehouse = getLocalDateTime(row.getCell(warehouseNameColumn + 1),row.getCell(warehouseNameColumn + 2)).toLocalTime(); //getLocalTimeCell(row.getCell(warehouseNameColumn + 2));

        if (ChronoUnit.DAYS.between(dateOfArrivingToCustomer, ZonedDateTime.now()) > 180 ||
                ChronoUnit.DAYS.between(dateOfArrivingToWarehouse, ZonedDateTime.now()) > 180) {
            tpa.setDeparturePlan(LocalDateTime.of(dateOfArrivingToCustomer, timeOfArrivingToCustomer).toString());
            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("TPA {} -> \n dateOfArrivingToCustomer {}, \n timeOfArrivingToCustomer {}, \n dateOfArrivingToWarehouse {}, \n timeOfArrivingToWarehouse {}",
                    tpa.getName(), dateOfArrivingToCustomer, timeOfArrivingToCustomer, dateOfArrivingToWarehouse, timeOfArrivingToWarehouse);
            return tpa;
        }

        // Creation ZoneDateTime of the moment when Manifest Should arrive to customer
        // based on LocalTime and ZoneId of Customer
        log.info("dateTimeETA = Date {}, Time {}", dateOfArrivingToCustomer, timeOfArrivingToCustomer);
        ZonedDateTime dateTimeETA = ZonedDateTime.of(getLocalDateTime(row.getCell(customerNameColumn + 1),
                row.getCell(customerNameColumn + 2)), ZoneId.of(customer.getTimeZone()));

        // Creation ZoneDateTime of the moment when Manifest Should arrive to warehouse
        // based on LocalTime and ZoneId of Warehouse
        log.info("dateTimeTxdEta = Date {}, Time {}", dateOfArrivingToWarehouse, timeOfArrivingToWarehouse);
        ZonedDateTime dateTimeTxdEta = ZonedDateTime.of(getLocalDateTime(row.getCell(warehouseNameColumn + 1),
                row.getCell(warehouseNameColumn + 2)), ZoneId.of(warehouse.getTimeZone()));

        // Creation ZoneDateTime of the moment when Manifest Should be released from Warehouse
        // based on Warehouse_Customer TransitTime and dateTimeETA and timeZone of Warehouse
        ZonedDateTime dateTimeETD = dateTimeETA.minusMinutes(whCustomerService.getTTminutes(whCustomer)).withZoneSameInstant(ZoneId.of(warehouse.getTimeZone()));
        log.info("Calculated time dateTimeETD = {} of departure from warehouse to reach planned arriving moment at Customer Warehouse {}", dateTimeETD, customer.getName());
        // IF all above calculated dates are in the past return tpa with status Error.
        if (dateTimeETA.isBefore(ZonedDateTime.now())
                || dateTimeTxdEta.isBefore(ZonedDateTime.now())
                || dateTimeETD.isBefore(ZonedDateTime.now())) {

            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("dateTimeETA {}, \n or dateTimeTxdEta {}, \n or dateTimeETD {} is in the Past", dateTimeETA, dateTimeTxdEta, dateTimeETD);
            return tpa;
        }

        // if amount of days between TPA ETD and ZoneDateTime.now() greater than 180 days, tpa status will
        // be assigned as Error
        if (ChronoUnit.DAYS.between(dateTimeETD, ZonedDateTime.now()) > 180) {
            tpa.setDeparturePlan(dateTimeETD.toLocalDateTime().toString());
            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("amount of days between TPA_ETD(departure) and ZoneDateTime.now() greater than 180 days");
            return tpa;
        }

        //Calculation of appropriate TPA and ZoneTimeDateStamp when the manifest will be sent
        Map.Entry<ZonedDateTime, TpaDaysSetting> calculatedETD = truckService.getAppropriateTpaSetting(dateTimeETD, whCustomer).entrySet().iterator().next();
        TpaDaysSetting appropriateTpa = calculatedETD.getValue();
        ZonedDateTime calculatedDateTimeETD = calculatedETD.getKey();
        log.info("calculatedDateTimeETD = {}", calculatedDateTimeETD);

        //if calculated ETD is before ETA of manifest arriving to warehouse set TPA status Error
        if (calculatedDateTimeETD.isBefore(dateTimeTxdEta)) {
            tpa.setDeparturePlan(calculatedDateTimeETD.toLocalDateTime().toString());
            tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.ERROR));
            log.info("TPA \"{}\" error: calculated ETD({}) is before ETA({}) of manifest arriving to warehouse \"{}\"",
                    tpa.getName(), calculatedDateTimeETD, dateTimeTxdEta, whCustomer.getCustomer().getName());

            return tpa;
        }

        tpa.setDeparturePlan(calculatedDateTimeETD.toLocalDateTime().toString());
        tpa.setTpaDaysSetting(appropriateTpa);
        tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.BUFFER));
        log.info("TPA with name [{}] COLLECTED ----------------------------", tpa.getName());
        return tpa;
    }

    private Set<TruckTimeTable> getListOfTTT(Row row) {
        Set<TruckTimeTable> tttList = new HashSet<>();
        TruckTimeTable directTTT = getStringFromCell(row.getCell(3)) != null
                ? getDirectTTT(row)
                : null;
        TruckTimeTable ccTTT = getStringFromCell(row.getCell(7)) != null
                ? getCcTTT(row)
                : null;
        TruckTimeTable xdTTT = getStringFromCell(row.getCell(11)) != null
                ? getTTT(row, 11, 12)
                : null;
        tttList.add(directTTT);
        tttList.add(ccTTT);
        tttList.add(xdTTT);
        return tttList.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private TruckTimeTable getCcTTT(Row row) {
        int ccTruckNameColumn = 7;
        int xdNameColumn = 8;
        int txdNameColumn = 12;
        TruckTimeTable ccTTT;
        if (getStringFromCell(row.getCell(xdNameColumn)) != null) {
            ccTTT = getTTT(row, ccTruckNameColumn, xdNameColumn);
        } else if (getStringFromCell(row.getCell(txdNameColumn)) != null) {
            ccTTT = getTTT(row, ccTruckNameColumn, txdNameColumn);
        } else {
            ccTTT = null;
        }
        return ccTTT;
    }

    private TruckTimeTable getDirectTTT(Row row) {
        int directTruckNameColumn = 3;
        int ccNameColumn = 4;
        int xdNameColumn = 8;
        int txdNameColumn = 12;
        TruckTimeTable directTTT;
        if (getStringFromCell(row.getCell(ccNameColumn)) != null) {
            directTTT = getTTT(row, directTruckNameColumn, ccNameColumn);
        } else if (getStringFromCell(row.getCell(xdNameColumn)) != null) {
            directTTT = getTTT(row, directTruckNameColumn, xdNameColumn);
        } else if (getStringFromCell(row.getCell(txdNameColumn)) != null) {
            directTTT = getTTT(row, directTruckNameColumn, txdNameColumn);
        } else {
            directTTT = null;
        }
        return directTTT;
    }

    private TruckTimeTable getTTT(Row row, int truckNameColumn, int warehouseNameColumn) {
        TruckTimeTable ttt = new TruckTimeTable();
        if (getStringFromCell(row.getCell(warehouseNameColumn)) != null) {
            String warehouseName = getStringFromCell(row.getCell(warehouseNameColumn));
            Warehouse warehouse = warehouseService.getWarehouseByName(warehouseName);
            LocalDateTime etaDateTime = getLocalDateTime(row.getCell(warehouseNameColumn + 1), row.getCell(warehouseNameColumn + 2));
            String truckName = getStringFromCell(row.getCell(truckNameColumn));
            ttt.setWarehouse(warehouse);
            ttt.setTruckName(truckName);
            ttt.setTttArrivalDatePlan(etaDateTime.toString());
            if (LocalDateTime.now().isAfter(etaDateTime)) {
                ttt.setTttStatus(truckService.getTttService().getTttStatusByEnum(TTTEnum.DELAYED));
            } else {
                ttt.setTttStatus(truckService.getTttService().getTttStatusByEnum(TTTEnum.PENDING));
            }
            if (ChronoUnit.DAYS.between(etaDateTime, LocalDateTime.now()) > 180) {
                ttt.setTttStatus(truckService.getTttService().getTttStatusByEnum(TTTEnum.ERROR));
            }
        } else {
            ttt = null;
        }
        return ttt;
    }

    /**
     * @param reference - taken from database and parsed from method @code getSetManifestReference
     * @param row       - current row in iteration from method getSetManifestReference represents sheet REFERENCE_FORECAST
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
    public Map<Long, ManifestTpaTttDTO> readSheet(Sheet sheet) {
        return null;
    }

    // Empty
    // method
    @Override
    public void fillRowWithData(ManifestTpaTttDTO object, Row row, CellStyle style) {

    }
}
