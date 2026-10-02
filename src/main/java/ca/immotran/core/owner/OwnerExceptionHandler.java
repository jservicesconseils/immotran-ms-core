package ca.immotran.core.owner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les erreurs metier du module owner en reponses HTTP propres.
 * La validation des DTO (@Valid) est geree par GlobalExceptionHandler,
 * pas ici (voir son Javadoc).
 */
@RestControllerAdvice
public class OwnerExceptionHandler {

    @ExceptionHandler(OwnerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOwnerNotFound(OwnerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(OwnerAlreadyAttachedException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyAttached(OwnerAlreadyAttachedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(OwnerOrganizationMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleOrganizationMismatch(OwnerOrganizationMismatchException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getMessage()));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
