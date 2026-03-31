package com.solutionti.usuarios.exception;

import com.solutionti.usuarios.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex,
                                                                  HttpServletRequest request) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        ErrorResponse error = buildError(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex,
                                                                   HttpServletRequest request) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        ErrorResponse error = buildError(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex,
                                                                       HttpServletRequest request) {
        log.warn("Acesso não autorizado: {}", ex.getMessage());
        ErrorResponse error = buildError(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                     HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .collect(Collectors.toList());

        log.warn("Erro de validação com {} campo(s) inválido(s)", fieldErrors.size());

        ErrorResponse error = buildError(
            HttpStatus.BAD_REQUEST,
            "Validation Error",
            "Dados de entrada inválidos. Verifique os campos informados.",
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                  HttpServletRequest request) {
        log.error("Erro interno não esperado: {}", ex.getMessage(), ex);
        ErrorResponse error = buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "Ocorreu um erro interno. Por favor, tente novamente mais tarde.",
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorResponse buildError(HttpStatus status,
                                      String error,
                                      String message,
                                      String path,
                                      List<ErrorResponse.FieldError> fieldErrors) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            error,
            message,
            path,
            fieldErrors
        );
    }
}
