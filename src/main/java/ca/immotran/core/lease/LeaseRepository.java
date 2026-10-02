package ca.immotran.core.lease;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    List<Lease> findByUnitId(UUID unitId);

    // Utilise par le dashboard (§20 "Baux a echeance") : baux actifs d'une
    // organisation dont la fin tombe dans la fenetre donnee.
    @Query("""
            SELECT COUNT(l) FROM Lease l
            WHERE l.unit.property.organizationId = :organizationId
              AND l.status = ca.immotran.core.lease.LeaseStatus.ACTIVE
              AND l.endDate BETWEEN :from AND :to
            """)
    long countActiveExpiringBetween(UUID organizationId, LocalDate from, LocalDate to);
}
