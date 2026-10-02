package ca.immotran.core.maintenance;

import java.util.UUID;

/**
 * Levee quand aucune demande de maintenance ne correspond a l'id demande
 * pour la propriete donnee. Traduite en reponse HTTP 404 (Not Found).
 */
public class MaintenanceRequestNotFoundException extends RuntimeException {

    public MaintenanceRequestNotFoundException(UUID id) {
        super("Aucune demande de maintenance trouvee avec l'id " + id);
    }
}
