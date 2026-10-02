package ca.immotran.core.property;

import java.util.UUID;

/**
 * Levee quand aucune propriete ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par PropertyExceptionHandler.
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(UUID id) {
        super("Aucune propriete trouvee avec l'id " + id);
    }
}
