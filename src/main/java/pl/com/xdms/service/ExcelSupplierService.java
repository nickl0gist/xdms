package pl.com.xdms.service;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created on 18.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class ExcelSupplierService implements ExcelService{
    @Override
    public Map readExcel(File file) {
        return null;
    }

    @Override
    public Map readSheet(Sheet sheet) {
        return null;
    }

    @Override
    public ByteArrayInputStream instanceToExcelFromTemplate(List objList) throws IOException {
        return null;
    }

    @Override
    public void fillRowWithData(Object object, Row row, CellStyle style) {

    }
}
