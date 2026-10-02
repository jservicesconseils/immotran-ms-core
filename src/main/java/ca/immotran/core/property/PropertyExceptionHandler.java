package ca.immotran.core.property;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les erreurs metier/validation du module property (Property et
 * Unit) en reponses HTTP propres, plutot que de laisser Spring renvoyer
 * une trace d'exception brute.
 */
@RestControllerAdvice
public class PropertyExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyNotFound(PropertyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUnitNotFound(UnitNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("requete invalide");
        return ResponseEntity.badRequest().body(errorBody(message));
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return body;
    }
}
