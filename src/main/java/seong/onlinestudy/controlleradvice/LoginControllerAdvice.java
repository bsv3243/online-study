package seong.onlinestudy.controlleradvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seong.onlinestudy.controller.LoginController;
import seong.onlinestudy.exception.BadCredentialException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice(assignableTypes = LoginController.class)
public class LoginControllerAdvice {

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(BadCredentialException.class)
    public ErrorResult badCredentialException(BadCredentialException ex) {
        log.error("[exception handle] ex", ex);
        return new ErrorResult("UNAUTHORIZED", ex.getMessage());
    }
}
