package ca.immotran.core.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Indicateurs de portefeuille pour une organisation (cahier des charges, §20). */
public record DashboardResponse(
        UUID organizationId,
        long totalProperties,
        long totalUnits,
        long occupiedUnits,
        long vacantUnits,
        long openMaintenanceRequests,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        long leasesExpiringNext30Days,
        long openApplications,
        List<RecentApplicationResponse> recentApplications
) {
}
