package ca.immotran.core.property.dto;

import ca.immotran.core.property.Unit;
import ca.immotran.core.property.UnitStatus;
import ca.immotran.core.property.UnitType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Vue PUBLIQUE minimale d'une unite, destinee a un candidat non
 * authentifie qui consulte l'annonce avant de postuler (voir
 * PropertyController.getUnitListing). Ne contient jamais organizationId
 * ni d'autres champs internes -- uniquement ce qu'une annonce affiche.
 */
public record UnitListingResponse(
        UUID propertyId,
        UUID unitId,
        String unitLabel,
        UnitType type,
        Integer bedrooms,
        Integer bathrooms,
        Double areaSquareMeters,
        BigDecimal listedRent,
        String propertyStreet,
        String propertyCity,
        String propertyProvince,
        UnitStatus status
) {

    public static UnitListingResponse from(Unit unit) {
        return new UnitListingResponse(
                unit.getProperty().getId(),
                unit.getId(),
                unit.getLabel(),
                unit.getType(),
                unit.getBedrooms(),
                unit.getBathrooms(),
                unit.getAreaSquareMeters(),
                unit.getListedRent(),
                unit.getProperty().getStreet(),
                unit.getProperty().getCity(),
                unit.getProperty().getProvince(),
                unit.getStatus());
    }
}
