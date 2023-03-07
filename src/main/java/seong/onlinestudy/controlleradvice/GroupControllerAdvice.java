package seong.onlinestudy.controlleradvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seong.onlinestudy.controller.GroupController;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice(assignableTypes = GroupController.class)
public class GroupControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoSuchElementException.class)
    public ErrorResult noSuchElementExHandle(NoSuchElementException ex) {
        log.error("[exception handle] ex", ex);
        return new ErrorResult("BAD_REQUEST", ex.getMessage());
    }
}
