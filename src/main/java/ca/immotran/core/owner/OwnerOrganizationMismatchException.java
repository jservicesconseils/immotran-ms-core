package ca.immotran.core.owner;

import java.util.UUID;

/**
 * Levee quand on tente d'associer un proprietaire a une propriete qui
 * n'appartient pas a la meme organisation (tenant) que lui -- une
 * validation metier en plus de la verification de tenant_id de
 * l'appelant (defense en profondeur). Traduite en reponse HTTP 409
 * (Conflict) par OwnerExceptionHandler.
 */
public class OwnerOrganizationMismatchException extends RuntimeException {

    public OwnerOrganizationMismatchException(UUID ownerId, UUID propertyId) {
        super("Le proprietaire " + ownerId + " n'appartient pas a la meme organisation que la propriete " + propertyId);
    }
}
