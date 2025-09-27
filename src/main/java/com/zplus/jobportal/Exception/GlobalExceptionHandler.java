package com.zplus.jobportal.Exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ApiError.class)
    public ResponseEntity<?> handleApiError(ApiError ex, HttpServletRequest request){
        ApiErrorResponse response = new ApiErrorResponse(
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
