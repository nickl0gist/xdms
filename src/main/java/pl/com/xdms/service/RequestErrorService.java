package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class RequestErrorService {

    private Map<String, String> getErrors(BindingResult bindingResult) {
        int i = 0;
        Map<String, String> errors = new HashMap<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            String key = error.getObjectName() +"-" + ((FieldError) error).getField() + "_";
            key += error.getCode();
            log.warn(key);
            String value = error.getDefaultMessage();
            log.warn(value);
            errors.put(key, value);
        }
        return errors;
    }

    /**
     * If reference from Put/Post (updating/creating) request has validation errors
     * the method will create response headers from BindingResult errors.
     * @param bindingResult keeps errors occurred after reference was checked
     * @return Response entity with reference in body and errors as http headers.
     */
    public HttpHeaders getErrorHeaders(BindingResult bindingResult){
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> map = getErrors(bindingResult);
        log.warn("Errors : {}", map.entrySet());
        headers.setAll(map);
        return headers;
    }
}