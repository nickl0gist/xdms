package pl.com.xdms.service.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

/**
 * Created on 12.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface ExcelService<T> {

    /**
     * @param file - Path to the file .xlsx which was sent by the user. This file should contain T objects to be
     *                 updated or saved in database
     * @return - List of T object
     */
    Map<Long, T> readExcel(File file);

    /**
     * @param sheet - instance of Excel Sheet from Workbook which was sent by user.
     * @return Map of T Objects as values with Row numbers as Keys. Initialized from given sheet.
     */
    Map<Long, T> readSheet(Sheet sheet);

    /**
     * Method is used to make user be able to download T Objects base in .xlsx file.
     * @return ByteArrayInputStream with template filled by the information from DB.
     * The file will be filled starting from index pointed in *rowIdx*
     */
    ByteArrayInputStream instanceToExcelFromTemplate() throws IOException;

    /**
     * @param object - values of properties T object will be parsed into cell values of the row
     * @param row - particular row where the properties of T object will be inserted
     * @param style - CellStyle from getXssfCellStyle() method
     */
    void fillRowWithData(T object, Row row, CellStyle style);

    /**
     * @param workbook - instance of Excel Workbook where Cell styles should be implemented
     * @return CellStyle - generated style
     */
    default XSSFCellStyle getXssfCellStyle(XSSFWorkbook workbook) {
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

    /**
     * Utility method to get cell value based on cell type
     * @param cell - Cell Entity.
     * @return Object to be parsed into Cell value
     */
    default Object getValueFromCell(Cell cell) {
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
     * The method used for parsing of given file to excel file.
     * @param file to be parsed to Workbook
     * @return map with entities of file is ok, empty map if any Exception was occurred.
     */
    default Map<Long, T> readFile(File file, String sheetName){
        try(Workbook workbook = WorkbookFactory.create(file)) {
            //get excel workbook
            Sheet sheet = workbook.getSheet(sheetName);
            return readSheet(sheet);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    /**
     * Methods which are converting cell values to Java type values
     * @param cell Cell entity from Excel
     * @return Java Type values
     */
    default Boolean getBooleanFromCell(Cell cell){
        return (cell.getCellType() == CellType.BOOLEAN)
                ? (Boolean) getValueFromCell(cell)
                : false;
    }

    default String getStringFromNumericCell(Cell cell) {
        return (cell.getCellType() == CellType.NUMERIC)
                ? ((Double) getValueFromCell(cell)).longValue() + ""
                : cell.getStringCellValue() + "";
    }

    default LocalDate getDateFromCell(Cell cell){
        return (DateUtil.isCellDateFormatted(cell))
                ? cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();
    }

    default Double getDoubleFromCell(Cell cell) {
        return (cell.getCellType() == CellType.NUMERIC)
                ? (Double) getValueFromCell(cell)
                : 0.0;
    }

    default String getStringFromCell(Cell cell) {
        return (cell.getCellType() != CellType.STRING)
                ? null
                : (String) getValueFromCell(cell);
    }

    default Long getLongFromCell(Cell cell) {
        return (((Double) getValueFromCell(cell)).longValue() == 0)
                ? null
                : ((Double) getValueFromCell(cell)).longValue();
    }

    default LocalDate getLocalDateCell (Cell cell){
        LocalDate date = null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            date = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return date;
    }

    default LocalTime getLocalTimeCell (Cell cell){
        LocalTime time = null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            time = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }
        return time;
    }

    default LocalDateTime getLocalDateTime (Cell date, Cell time){
        LocalDate dateETA = getLocalDateCell(date);
        LocalTime timeETA = getLocalTimeCell(time);
        LocalDateTime dateTimeETA = LocalDateTime.of(1900,1,1,0,0);
        if(dateETA != null && timeETA != null){
            dateTimeETA = LocalDateTime.of(dateETA, timeETA);
        }
        return dateTimeETA;
    }

}
