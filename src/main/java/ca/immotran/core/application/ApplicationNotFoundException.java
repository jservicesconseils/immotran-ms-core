package ca.immotran.core.application;

import java.util.UUID;

/**
 * Levee quand aucune candidature ne correspond a l'id demande pour
 * l'unite donnee. Traduite en reponse HTTP 404 (Not Found).
 */
public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(UUID id) {
        super("Aucune candidature trouvee avec l'id " + id);
    }
}
