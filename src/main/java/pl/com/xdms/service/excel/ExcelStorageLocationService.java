package pl.com.xdms.service.excel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.service.StorageLocationService;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created on 13.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
@Slf4j
public class ExcelStorageLocationService implements ExcelService<StorageLocation> {

    public ExcelProperties excelProperties;
    public final StorageLocationService storageLocationService;

    @Autowired
    public ExcelStorageLocationService(ExcelProperties excelProperties,
                                       StorageLocationService storageLocationService) {
        this.excelProperties = excelProperties;
        this.storageLocationService = storageLocationService;
    }

    @Override
    public Map<Long, StorageLocation> readExcel(File file) {
        log.info("File with SLs received {}", file.getPath());
        Map<Long, StorageLocation> map = readFile(file);
        if (map.isEmpty()) {
            log.error("Error occurred while reading the file with Storage Locations");
        }
        return map;
    }

    @Override
    public Map<Long, StorageLocation> readSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<Long, StorageLocation> storageLocationMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            StorageLocation storageLocation = new StorageLocation();
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
                        storageLocation.setStorageLocationID(getLongFromCell(cell));
                        break;
                    case 1:
                        storageLocation.setCode(getStringFromCell(cell));
                        break;
                    case 2:
                        storageLocation.setName(getStringFromCell(cell));
                        break;
                    case 3:
                        storageLocation.setIsActive(getBooleanFromCell(cell)) ;
                        break;
                }
                storageLocationMap.put(row.getRowNum() + 1L, storageLocation);
            }
            log.info("Storage Location parsed {}", storageLocation);
        }
        return storageLocationMap;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate(){

        List<StorageLocation> storageLocationList = storageLocationService.getAllStorLocs();

        try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(
                new FileInputStream(excelProperties.getPathToStorageLocationTemplate()));
                //new FileInputStream(excelProperties.getPathToStorageLocationTemplate().getFile()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheet(excelProperties.getStorageLocationsSheetName());
            int rowIdx = 2;
            CellStyle style = getXssfCellStyle(workbook);
            for (StorageLocation stLoc : storageLocationList) {
                Row row = sheet.createRow(rowIdx++);
                fillRowWithData(stLoc, row, style);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.warn("Error occurred while creating file with St.Locks from database: {}",e.getMessage());
            return null;
        }
    }

    @Override
    public void fillRowWithData(StorageLocation storageLocation, Row row, CellStyle style) {

         Cell idCell = row.createCell(0);
         idCell.setCellValue(storageLocation.getStorageLocationID());
         idCell.setCellStyle(style);

         Cell codeCell = row.createCell(1);
         codeCell.setCellValue(storageLocation.getCode());
         codeCell.setCellStyle(style);

         Cell nameCell = row.createCell(2);
         nameCell.setCellValue(storageLocation.getName());
         nameCell.setCellStyle(style);

        Cell isActiveCell = row.createCell(3);
        isActiveCell.setCellValue(true);
        isActiveCell.setCellStyle(style);

    }

}
