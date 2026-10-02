package ca.immotran.core.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    // Utilise par le dashboard (§20 "Proprietes et unites").
    long countByOrganizationId(UUID organizationId);
}
