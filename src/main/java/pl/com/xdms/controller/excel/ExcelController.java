package pl.com.xdms.controller.excel;

import org.slf4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.service.excel.ExcelService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    /**
     * @param key - index of Row in given Excel file
     * @param entity - entity created from paticular row from Excel
     * @param log - Logger given from controller
     * @return boolean result of validation, if Entity is not valid - false, if valid - true.
     */
    default boolean validation(Long key, T entity, Logger log) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        log.info(entity.toString());
        Set<ConstraintViolation<T>> constraintValidator = validator.validate(entity);
        if (!constraintValidator.isEmpty()) {
            log.info("Row {} would not be persisted: {}", key, constraintValidator);
            return false;
        }
        return true;
    }

    /**
     * @param entityList - List of entities to be recorded onto Excel file.
     * @param excelEntityService - ExcelService implementation entity which will create the file with entities
     * @return - Response Entity with the file and created headers.
     * @throws IOException exception if it will appear.
     */
    default ResponseEntity<InputStreamSource> getInputStreamSourceResponseEntity(List<T> entityList,
                                                                                 ExcelService excelEntityService) throws IOException {
        ByteArrayInputStream in = excelEntityService.instanceToExcelFromTemplate(entityList);
        in.close();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=storage_locations.xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
