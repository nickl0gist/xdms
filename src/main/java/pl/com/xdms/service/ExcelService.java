package pl.com.xdms.service;

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.domain.reference.Reference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

/**
 * Created on 26.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Data
public class ExcelService {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelService.class);

    private final ExcelProperties referenceBase;
    private final String sheetNameForReferences;

    @Autowired
    public ExcelService(ExcelProperties referenceBase) {
        this.referenceBase = referenceBase;
        this.sheetNameForReferences = referenceBase.getSheetName();
    }

    public ByteArrayInputStream referencesToExcel(List<Reference> references) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CreationHelper creationHelper = workbook.getCreationHelper();
            XSSFSheet sheet = workbook.createSheet(sheetNameForReferences);
            LOG.info(referenceBase.toString());
            // Set which area the table should be placed in
            AreaReference areaReference = creationHelper.createAreaReference(
                    new CellReference(0, 0), new CellReference(references.size(), referenceBase.getColumns().size() - 1));

            // Create table
            XSSFTable table = sheet.createTable(areaReference);
            table.setName(sheetNameForReferences);
            table.setDisplayName(sheetNameForReferences);

            // For now, create the initial style in a low-level way
            table.getCTTable().addNewTableStyleInfo();
            table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

            // Style the table
            XSSFTableStyleInfo tableStyle = (XSSFTableStyleInfo) table.getStyle();
            tableStyle.setShowColumnStripes(false);
            tableStyle.setShowRowStripes(true);
            tableStyle.setFirstColumn(false);
            tableStyle.setLastColumn(false);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);


            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(THIN);
            cellStyle.setBottomBorderColor(IndexedColors.BLUE.getIndex());
            cellStyle.setBorderTop(THIN);
            cellStyle.setTopBorderColor(IndexedColors.BLUE.getIndex());
            cellStyle.setBorderLeft(THIN);
            cellStyle.setLeftBorderColor(IndexedColors.BLUE.getIndex());
            cellStyle.setBorderRight(THIN);
            cellStyle.setRightBorderColor(IndexedColors.BLUE.getIndex());

            // Row for Header
            Row headerRow = sheet.createRow(0);
            headerRow.setHeight((short) 600);

            int index = 0;
            for (Map.Entry<String, Integer> entry : referenceBase.getColumns().entrySet()){
                Cell cell = headerRow.createCell(index);
                sheet.setColumnWidth(index, entry.getValue()*referenceBase.getColumnWidthIndex());
                cell.setCellValue(entry.getKey().substring(entry.getKey().lastIndexOf('.')+1));
                cell.setCellStyle(headerCellStyle);
                cell.setCellStyle(cellStyle);
                index++;
            }

            int rowIdx = 1;

            for (Reference reference : references) {
                Row row = sheet.createRow(rowIdx++);
                //ID
                row.createCell(0).setCellStyle(cellStyle);
                row.getCell(0).setCellValue(reference.getReferenceID());
                //NUMBER
                row.createCell(1).setCellStyle(cellStyle);
                row.getCell(1).setCellValue(reference.getNumber());
                //NAME
                row.createCell(2).setCellStyle(cellStyle);
                row.getCell(2).setCellValue(reference.getName());
                //DESIGNATION EN
                row.createCell(3).setCellStyle(cellStyle);
                row.getCell(3).setCellValue(reference.getDesignationEN());
                //DESIGNATION RU
                row.createCell(4).setCellStyle(cellStyle);
                row.getCell(4).setCellValue(reference.getDesignationRU());
                //HSCODE
                row.createCell(5).setCellStyle(cellStyle);
                row.getCell(5).setCellValue(reference.getHsCode());
                //WEIGHT
                row.createCell(6).setCellStyle(cellStyle);
                row.getCell(6).setCellValue(reference.getWeight());
                //WEIGHT PU
                row.createCell(7).setCellStyle(cellStyle);
                row.getCell(7).setCellValue(reference.getWeightPu());
                //WEIGHT HU
                row.createCell(8).setCellStyle(cellStyle);
                row.getCell(8).setCellValue(reference.getWeightHu());
                //STACKABILITI
                row.createCell(9).setCellStyle(cellStyle);
                row.getCell(9).setCellValue(reference.getStackability());
                // PCS PER PU
                row.createCell(10).setCellStyle(cellStyle);
                row.getCell(10).setCellValue(reference.getPcsPerPU());
                // PCS PER HU
                row.createCell(11).setCellStyle(cellStyle);
                row.getCell(11).setCellValue(reference.getPcsPerHU());
                // PALLET WEIGHT
                row.createCell(12).setCellStyle(cellStyle);
                row.getCell(12).setCellValue(reference.getPalletWeight());
                // PALLET HEIGHT
                row.createCell(13).setCellStyle(cellStyle);
                row.getCell(13).setCellValue(reference.getPalletHeight());
                // PALLET LENGTH
                row.createCell(14).setCellStyle(cellStyle);
                row.getCell(14).setCellValue(reference.getPalletLength());
                // PALLET WIDTH
                row.createCell(15).setCellStyle(cellStyle);
                row.getCell(15).setCellValue(reference.getPalletWidth());
                // SUPPLIER
                // CUSTOMER
                // SUPPLIER AGREEMENT
                // CUSTOMER AGREEMENT
                // STORAGE LOCATION
                // IS ACTIVE
                row.createCell(21).setCellStyle(cellStyle);
                row.getCell(21).setCellValue(reference.getIsActive());

            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
