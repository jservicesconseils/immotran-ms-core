package ca.immotran.core.lease;

import java.util.UUID;

/**
 * Levee quand aucun bail ne correspond a l'id demande pour l'unite donnee.
 * Traduite en reponse HTTP 404 (Not Found) par LeaseExceptionHandler.
 */
public class LeaseNotFoundException extends RuntimeException {

    public LeaseNotFoundException(UUID id) {
        super("Aucun bail trouve avec l'id " + id);
    }
}
