package ca.immotran.core.property;

import ca.immotran.core.property.dto.CreatePropertyRequest;
import ca.immotran.core.property.dto.PropertyResponse;
import ca.immotran.core.property.dto.UpdatePropertyRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de base sur Property. Ne contient aucune logique de securite :
 * le controleur verifie le tenant_id via TenantClaims avant/apres appel,
 * ce service reste testable sans dependre du contexte de securite.
 */
@Service
public class PropertyService {

    private final PropertyRepository repository;

    public PropertyService(PropertyRepository repository) {
        this.repository = repository;
    }

    public PropertyResponse create(CreatePropertyRequest request) {
        Property saved = repository.save(new Property(
                request.organizationId(),
                request.type(),
                request.name(),
                request.street(),
                request.city(),
                request.province(),
                request.postalCode(),
                request.cadastreNumber(),
                request.taxId(),
                request.buildingStatus(),
                request.yearBuilt(),
                request.floorCount(),
                request.totalSurfaceArea(),
                request.estimatedValue(),
                request.description()));
        return PropertyResponse.from(saved);
    }

    public PropertyResponse getById(UUID id) {
        return PropertyResponse.from(getEntityById(id));
    }

    public PropertyResponse update(UUID id, UpdatePropertyRequest request) {
        Property property = getEntityById(id);
        property.update(
                request.type(),
                request.name(),
                request.street(),
                request.city(),
                request.province(),
                request.postalCode(),
                request.cadastreNumber(),
                request.taxId(),
                request.buildingStatus(),
                request.yearBuilt(),
                request.floorCount(),
                request.totalSurfaceArea(),
                request.estimatedValue(),
                request.description());
        return PropertyResponse.from(repository.save(property));
    }

    public List<PropertyResponse> listByOrganization(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(PropertyResponse::from)
                .toList();
    }

    // Public (et pas seulement package-private) volontairement : d'autres
    // modules du monolithe (owner, puis lease/maintenance) ont besoin de
    // l'entite Property brute pour leurs propres relations JPA. Les
    // controleurs, eux, ne manipulent que des DTO (PropertyResponse).
    public Property getEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException(id));
    }
}
