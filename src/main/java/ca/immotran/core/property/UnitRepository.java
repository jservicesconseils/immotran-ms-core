package ca.immotran.core.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    List<Unit> findByPropertyId(UUID propertyId);

    // Utilises par le dashboard (§20 "Occupation/vacance", "Taux d'occupation").
    @Query("SELECT COUNT(u) FROM Unit u WHERE u.property.organizationId = :organizationId")
    long countByOrganizationId(UUID organizationId);

    @Query("SELECT COUNT(u) FROM Unit u WHERE u.property.organizationId = :organizationId AND u.status = :status")
    long countByOrganizationIdAndStatus(UUID organizationId, UnitStatus status);
}
