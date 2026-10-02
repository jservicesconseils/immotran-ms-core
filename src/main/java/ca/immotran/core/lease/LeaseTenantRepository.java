package ca.immotran.core.lease;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaseTenantRepository extends JpaRepository<LeaseTenant, UUID> {

    List<LeaseTenant> findByLeaseId(UUID leaseId);
}
