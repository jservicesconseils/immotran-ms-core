package ca.immotran.core.owner;

import java.util.UUID;

/**
 * Levee quand aucun proprietaire ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par OwnerExceptionHandler.
 */
public class OwnerNotFoundException extends RuntimeException {

    public OwnerNotFoundException(UUID id) {
        super("Aucun proprietaire trouve avec l'id " + id);
    }
}
