package ca.immotran.core.admin;

import java.util.UUID;

/**
 * Levee quand aucune regle juridictionnelle ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par AdminExceptionHandler.
 */
public class JurisdictionRuleNotFoundException extends RuntimeException {

    public JurisdictionRuleNotFoundException(UUID id) {
        super("Aucune regle juridictionnelle trouvee avec l'id " + id);
    }
}
