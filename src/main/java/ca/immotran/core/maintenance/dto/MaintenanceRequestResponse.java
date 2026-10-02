package ca.immotran.core.maintenance.dto;

import ca.immotran.core.maintenance.MaintenancePriority;
import ca.immotran.core.maintenance.MaintenanceRequest;
import ca.immotran.core.maintenance.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'une demande de maintenance. */
public record MaintenanceRequestResponse(
        UUID id,
        UUID propertyId,
        UUID unitId,
        String description,
        MaintenancePriority priority,
        MaintenanceStatus status,
        String vendorName,
        BigDecimal cost,
        Instant createdAt,
        Instant closedAt
) {

    public static MaintenanceRequestResponse from(MaintenanceRequest request) {
        return new MaintenanceRequestResponse(
                request.getId(),
                request.getProperty().getId(),
                request.getUnit() != null ? request.getUnit().getId() : null,
                request.getDescription(),
                request.getPriority(),
                request.getStatus(),
                request.getVendorName(),
                request.getCost(),
                request.getCreatedAt(),
                request.getClosedAt());
    }
}
