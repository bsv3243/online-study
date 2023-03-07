package seong.onlinestudy.controlleradvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.exception.PermissionControlException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidSessionException.class)
    public ErrorResult invalidSessionExHandle(InvalidSessionException ex) {
        log.error("[exception handle] ex", ex);
        return new ErrorResult("UNAUTHORIZED", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResult methodArgumentNotValidEx(MethodArgumentNotValidException ex) {
        log.error("[exception handle] ex", ex);
        ErrorResult result = new ErrorResult();
        result.setCode("BAD_REQUEST");
        for (FieldError fieldError : ex.getFieldErrors()) {
            result.getMessage().add(fieldError.getDefaultMessage());
        }

        return result;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(PermissionControlException.class)
    public ErrorResult permissionControlExHandle(PermissionControlException ex) {
        log.error("[exception handle] ex", ex);
        return new ErrorResult("FORBIDDEN", ex.getMessage());
    }
}
