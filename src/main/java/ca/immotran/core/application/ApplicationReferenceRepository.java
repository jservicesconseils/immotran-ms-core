package ca.immotran.core.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationReferenceRepository extends JpaRepository<ApplicationReference, UUID> {

    List<ApplicationReference> findByApplicationId(UUID applicationId);
}
