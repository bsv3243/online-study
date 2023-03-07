package seong.onlinestudy.controlleradvice;

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

}
