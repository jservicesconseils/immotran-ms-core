package ca.immotran.core.tenant;

import java.util.UUID;

/**
 * Levee quand aucun locataire ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par TenantExceptionHandler.
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(UUID id) {
        super("Aucun locataire trouve avec l'id " + id);
    }
}
