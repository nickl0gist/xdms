package pl.com.xdms.service;

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${field.divider}")
    private String fieldDivider;

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

            // Set which area the table should be placed in
            AreaReference areaReference = creationHelper.createAreaReference(
                    new CellReference(0, 0),
                    new CellReference(references.size(), referenceBase.getColumns().size() - 1));

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

            Font rowFont = workbook.createFont();
            rowFont.setColor(IndexedColors.BLACK.getIndex());

            //Creating cell style
            XSSFCellStyle cellStyle = getXssfCellStyle(workbook);

            //Row for Header
            Row headerRow = sheet.createRow(0);
            headerRow.setHeight((short) 600);

            //Inserting header names into header row
            int index = 0;
            for (Map.Entry<String, Integer> entry : referenceBase.getColumns().entrySet()) {
                Cell cell = headerRow.createCell(index);
                sheet.setColumnWidth(index, entry.getValue() * referenceBase.getColumnWidthIndex());
                cell.setCellValue(entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1));
                cell.setCellStyle(cellStyle);
                cell.getCellStyle().setFont(getHeaderFont(workbook));
                index++;
            }

            //inserting values from references into rows starting from 1
            int rowIdx = 1;
            for (Reference reference : references) {
                String[] refAsList = reference.toStringForExcel(fieldDivider).split(fieldDivider);
                Row row = sheet.createRow(rowIdx++);
                row.setRowStyle(cellStyle);
                row.setRowStyle(workbook.createCellStyle());
                row.getRowStyle().setFont(rowFont);
                row.getRowStyle().setWrapText(false);
                for (int i = 0; i < refAsList.length; i++) {
                    row.createCell(i).setCellStyle(row.getRowStyle());
                    row.getCell(i).setCellValue(refAsList[i]);
                }
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Font getHeaderFont(XSSFWorkbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        return headerFont;
    }
    private XSSFCellStyle getXssfCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setBorderBottom(THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLUE.getIndex());
        cellStyle.setBorderTop(THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLUE.getIndex());
        cellStyle.setBorderLeft(THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLUE.getIndex());
        cellStyle.setBorderRight(THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLUE.getIndex());
        return cellStyle;
    }
}
