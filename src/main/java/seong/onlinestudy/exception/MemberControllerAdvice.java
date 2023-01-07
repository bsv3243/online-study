package seong.onlinestudy.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seong.onlinestudy.controller.MemberController;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes = MemberController.class)
public class MemberControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResult requestValidExHandle(MethodArgumentNotValidException ex) {
        log.error("[exceptionHandle] ex", ex);
        return new ErrorResult("BAD_REQUEST", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
