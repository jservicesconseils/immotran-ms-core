package ca.immotran.core.owner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {

    List<Owner> findByOrganizationId(UUID organizationId);
}
