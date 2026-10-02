package ca.immotran.core.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    List<Unit> findByPropertyId(UUID propertyId);
}
