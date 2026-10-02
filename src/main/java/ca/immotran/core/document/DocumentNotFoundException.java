package ca.immotran.core.document;

import java.util.UUID;

/**
 * Levee quand aucun document ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par DocumentExceptionHandler.
 */
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(UUID id) {
        super("Aucun document trouve avec l'id " + id);
    }
}
