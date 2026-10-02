package ca.immotran.core.owner;

import java.util.UUID;

/**
 * Levee quand on tente d'associer un proprietaire a une propriete a
 * laquelle il est deja associe. Traduite en reponse HTTP 409 (Conflict)
 * par OwnerExceptionHandler.
 */
public class OwnerAlreadyAttachedException extends RuntimeException {

    public OwnerAlreadyAttachedException(UUID ownerId, UUID propertyId) {
        super("Le proprietaire " + ownerId + " est deja associe a la propriete " + propertyId);
    }
}
