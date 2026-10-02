package ca.immotran.core;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Erreurs transverses a TOUS les modules (property, owner, puis les
 * suivants) : la validation des DTO (@Valid) n'est pas une regle metier
 * d'un domaine particulier. Gardee a un seul endroit pour eviter une
 * ambiguite de mapping si plusieurs @RestControllerAdvice geraient la
 * meme exception.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("requete invalide");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }
}
