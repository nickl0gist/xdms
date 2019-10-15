package pl.com.xdms.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public class UserExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = UserException.class)
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request){
        String bodyOfResponse = "Such user does not exist!";
        return handleExceptionInternal(ex, null,
                new HttpHeaders(), UNPROCESSABLE_ENTITY, request);
    }
}
