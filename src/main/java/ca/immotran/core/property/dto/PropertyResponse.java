package ca.immotran.core.property.dto;

import ca.immotran.core.property.Property;
import ca.immotran.core.property.PropertyStatus;
import ca.immotran.core.property.PropertyType;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'une propriete. */
public record PropertyResponse(
        UUID id,
        UUID organizationId,
        PropertyType type,
        String street,
        String city,
        String province,
        String postalCode,
        PropertyStatus status,
        Instant createdAt
) {

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getOrganizationId(),
                property.getType(),
                property.getStreet(),
                property.getCity(),
                property.getProvince(),
                property.getPostalCode(),
                property.getStatus(),
                property.getCreatedAt());
    }
}
