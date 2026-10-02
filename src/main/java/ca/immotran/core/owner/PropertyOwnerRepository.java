package ca.immotran.core.owner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyOwnerRepository extends JpaRepository<PropertyOwner, UUID> {

    List<PropertyOwner> findByPropertyId(UUID propertyId);

    boolean existsByPropertyIdAndOwnerId(UUID propertyId, UUID ownerId);
}
