package com.team3.otboo.global.exception;

import com.team3.otboo.domain.weather.dto.response.LocationErrorResponse;
import com.team3.otboo.global.exception.weather.ExternalApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("Validation failed: {} (request path: {})", ex.getMessage(), request.getRequestURI(), ex);
    return new ResponseEntity<>(
        ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, request.getRequestURI(), ex.getBindingResult()),
        ErrorCode.INVALID_INPUT_VALUE.getStatus()
    );
  }

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException ex, HttpServletRequest request) {
    log.warn("handleBusinessException: {} (request path: {})", ex.getMessage(), request.getRequestURI(), ex);
    final ErrorCode errorCode = ex.getErrorCode();
    final ErrorResponse response = ErrorResponse.of(errorCode, request.getRequestURI(), ex.getDetailMessage());
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
    log.error("handleException: {} (request path: {})", ex.getMessage(), request.getRequestURI(), ex);
    final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
    return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
  }

  @ExceptionHandler(ExternalApiException.class)
  public ResponseEntity<LocationErrorResponse> handleExternalApiException(ExternalApiException ex) {
    LocationErrorResponse response = LocationErrorResponse.of("EXTERNAL_API_FAILED", ex.getDetailMessage());
    return new ResponseEntity<>(response, ErrorCode.EXTERNAL_API_FAILED.getStatus());
  }

}
