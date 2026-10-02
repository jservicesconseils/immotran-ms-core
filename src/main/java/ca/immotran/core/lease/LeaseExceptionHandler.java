package ca.immotran.core.lease;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class LeaseExceptionHandler {

    @ExceptionHandler(LeaseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLeaseNotFound(LeaseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(LeaseTenantOrganizationMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleOrganizationMismatch(LeaseTenantOrganizationMismatchException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getMessage()));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
