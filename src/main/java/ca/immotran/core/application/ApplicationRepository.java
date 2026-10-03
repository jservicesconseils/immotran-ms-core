package ca.immotran.core.application;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByUnitId(UUID unitId);

    // Utilise par le dashboard (§20 "Dossiers en cours") : candidatures
    // pas encore decidees, toutes unites/proprietes confondues pour
    // l'organisation.
    @Query("""
            SELECT COUNT(a) FROM Application a
            WHERE a.unit.property.organizationId = :organizationId
              AND a.status NOT IN (ca.immotran.core.application.ApplicationStatus.ACCEPTEE,
                                    ca.immotran.core.application.ApplicationStatus.REFUSEE)
            """)
    long countOpenByOrganizationId(@Param("organizationId") UUID organizationId);

    // Utilise par le dashboard ("Dossiers recents") : les candidatures les
    // plus recemment soumises pour l'organisation, toutes unites confondues.
    @Query("""
            SELECT a FROM Application a
            WHERE a.unit.property.organizationId = :organizationId
            ORDER BY a.submittedAt DESC
            """)
    List<Application> findRecentByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);
}
