package pl.com.xdms.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.Map;

@Service
public class RequestErrorService {
    public Map<String, String> getErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            String key = error.getObjectName()+ " : " + error.getCode() + " ";
            key += ((FieldError) error).getField();
            String value = error.getDefaultMessage();
            errors.put(key, value);
        }
        return errors;
    }
}