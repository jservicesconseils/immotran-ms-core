package ca.immotran.core.lease.dto;

import ca.immotran.core.lease.Lease;
import ca.immotran.core.lease.LeaseStatus;
import ca.immotran.core.lease.LeaseTenant;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'un bail. */
public record LeaseResponse(
        UUID id,
        UUID propertyId,
        UUID unitId,
        List<UUID> tenantIds,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        Instant securityDepositPaidAt,
        LeaseStatus status,
        Instant createdAt
) {

    public static LeaseResponse from(Lease lease, List<LeaseTenant> leaseTenants) {
        List<UUID> tenantIds = leaseTenants.stream()
                .map(lt -> lt.getTenant().getId())
                .toList();

        return new LeaseResponse(
                lease.getId(),
                lease.getUnit().getProperty().getId(),
                lease.getUnit().getId(),
                tenantIds,
                lease.getStartDate(),
                lease.getEndDate(),
                lease.getMonthlyRent(),
                lease.getSecurityDeposit(),
                lease.getSecurityDepositPaidAt(),
                lease.getStatus(),
                lease.getCreatedAt());
    }
}
