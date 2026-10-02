package ca.immotran.core.property;

import java.util.UUID;

/**
 * Levee quand aucune unite ne correspond a l'id demande pour la propriete
 * donnee (y compris quand l'unite existe mais appartient a une AUTRE
 * propriete -- on ne distingue pas les deux cas, pour ne rien reveler).
 * Traduite en reponse HTTP 404 (Not Found) par PropertyExceptionHandler.
 */
public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException(UUID id) {
        super("Aucune unite trouvee avec l'id " + id);
    }
}
