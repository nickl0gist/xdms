package pl.com.xdms.controller;

import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created on 13.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

public interface ExcelController <T>{

    /**
     * The Endpoint is for downloading base records of <tt>T</tt> entity.
     * @return InputStreamSource of the file in .xlsx
     */
    ResponseEntity<InputStreamSource> downloadBase() throws IOException;

    /**
     * @param file .xlsx received from user with rows to be converted into <tt>T</tt> entity
     * @return List of validated <tt>T</tt> objects after parsing the file.
     * Not valid references will have status isAstive = false.
     */
    List<T> uploadFile(@RequestParam("file") MultipartFile file);

    /**
     * @param key number of Row of the specific <tt>T</tt> entity in given excel file staring from 1
     * @param entity mapped from Excel row to be validated
     * @return validated entity
     * Check if entity is Valid. If it isn`t isActive field will be set to false.
     */
    T entityValidation(Long key, T entity);

    /**
     * Controller saves Entities with isActive = true
     * @param objList to be persisted in Database
     * @return status "Created" and list of Entities from request with both statuses.
     */
    ResponseEntity<List<T>> saveAllEntities(@RequestBody List<T> objList);
}
