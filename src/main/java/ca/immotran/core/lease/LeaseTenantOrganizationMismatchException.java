package ca.immotran.core.lease;

import java.util.UUID;

/**
 * Levee quand on tente de mettre un locataire sur un bail dont l'unite
 * appartient a une AUTRE organisation que lui -- validation metier en
 * defense en profondeur, meme principe qu'OwnerOrganizationMismatchException
 * dans le module owner. Traduite en reponse HTTP 409 (Conflict).
 */
public class LeaseTenantOrganizationMismatchException extends RuntimeException {

    public LeaseTenantOrganizationMismatchException(UUID tenantId, UUID unitId) {
        super("Le locataire " + tenantId + " n'appartient pas a la meme organisation que l'unite " + unitId);
    }
}
