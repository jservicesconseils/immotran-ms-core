package ca.immotran.core.owner.dto;

import ca.immotran.core.owner.PropertyOwner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie apres association (ou lecture) d'un proprietaire sur une propriete. */
public record PropertyOwnerResponse(
        UUID id,
        UUID propertyId,
        UUID ownerId,
        String ownerName,
        BigDecimal sharePercentage,
        Instant createdAt
) {

    public static PropertyOwnerResponse from(PropertyOwner propertyOwner) {
        return new PropertyOwnerResponse(
                propertyOwner.getId(),
                propertyOwner.getProperty().getId(),
                propertyOwner.getOwner().getId(),
                propertyOwner.getOwner().getName(),
                propertyOwner.getSharePercentage(),
                propertyOwner.getCreatedAt());
    }
}
