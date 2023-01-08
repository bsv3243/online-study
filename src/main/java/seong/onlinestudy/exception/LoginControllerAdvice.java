package seong.onlinestudy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seong.onlinestudy.controller.LoginController;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice(assignableTypes = LoginController.class)
public class LoginControllerAdvice {

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(MemberNotFoundException.class)
    public ErrorResult memberNotFoundExHandle(MemberNotFoundException ex) {
        log.error("[exception handle] ex", ex);
        return new ErrorResult("UNAUTHORIZED", ex.getMessage());
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(BadPasswordException.class)
    public ErrorResult badPasswordExHandle(BadPasswordException ex) {
        log.error("[exception handle] ex", ex);
        return new ErrorResult("UNAUTHORIZED", ex.getMessage());
    }
}
