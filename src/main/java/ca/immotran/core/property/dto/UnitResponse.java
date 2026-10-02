package ca.immotran.core.property.dto;

import ca.immotran.core.property.Unit;
import ca.immotran.core.property.UnitStatus;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'une unite. */
public record UnitResponse(
        UUID id,
        UUID propertyId,
        String label,
        boolean principal,
        Integer floor,
        Double areaSquareMeters,
        Integer bedrooms,
        Integer bathrooms,
        UnitStatus status,
        Instant createdAt
) {

    public static UnitResponse from(Unit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getProperty().getId(),
                unit.getLabel(),
                unit.isPrincipal(),
                unit.getFloor(),
                unit.getAreaSquareMeters(),
                unit.getBedrooms(),
                unit.getBathrooms(),
                unit.getStatus(),
                unit.getCreatedAt());
    }
}
