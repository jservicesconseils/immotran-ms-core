package ca.immotran.core.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    List<MaintenanceRequest> findByPropertyId(UUID propertyId);

    // Utilise par le dashboard (§20 "Maintenance ouverte").
    @Query("""
            SELECT COUNT(m) FROM MaintenanceRequest m
            WHERE m.property.organizationId = :organizationId
              AND m.status IN (ca.immotran.core.maintenance.MaintenanceStatus.OUVERTE,
                                ca.immotran.core.maintenance.MaintenanceStatus.EN_COURS)
            """)
    long countOpenByOrganizationId(UUID organizationId);
}
