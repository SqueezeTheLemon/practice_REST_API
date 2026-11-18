package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

// 모든 컨트롤러에서 터진 예외를 한곳에서 받아서, RsData 모양의 응답으로 바꿔 줘요
@RestControllerAdvice
public class GlobalExceptionHandler {

    // findById(...).get() 했는데 값이 없을 때 → 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        return new ResponseEntity<>(
                new RsData<>(
                        "404-1",
                        "해당 데이터가 존재하지 않습니다."
                ),
                NOT_FOUND
        );
    }

    // @Valid 검증 실패 → 400, 어떤 필드가 어떤 규칙을 어겼는지 메시지에 담아요
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage()) // 예: title-NotBlank-공백일 수 없습니다
                .sorted()
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        msg
                ),
                BAD_REQUEST
        );
    }
}
