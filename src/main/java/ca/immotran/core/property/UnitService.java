package ca.immotran.core.property;

import ca.immotran.core.property.dto.CreateUnitRequest;
import ca.immotran.core.property.dto.UnitResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de base sur Unit, toujours au travers de sa Property parente.
 * Comme PropertyService, aucune logique de securite ici : le controleur
 * s'en charge via TenantClaims.
 */
@Service
public class UnitService {

    private final UnitRepository repository;
    private final PropertyService propertyService;

    public UnitService(UnitRepository repository, PropertyService propertyService) {
        this.repository = repository;
        this.propertyService = propertyService;
    }

    public UnitResponse create(UUID propertyId, CreateUnitRequest request) {
        Property property = propertyService.getEntityById(propertyId);
        Unit saved = repository.save(new Unit(
                property,
                request.label(),
                request.principal(),
                request.floor(),
                request.areaSquareMeters(),
                request.bedrooms(),
                request.bathrooms()));
        return UnitResponse.from(saved);
    }

    public UnitResponse getById(UUID propertyId, UUID unitId) {
        return UnitResponse.from(getEntityByIdAndProperty(unitId, propertyId));
    }

    public List<UnitResponse> listByProperty(UUID propertyId) {
        // Valide au passage que la propriete existe (404 sinon), avant de
        // renvoyer une liste vide pour un id totalement inconnu.
        propertyService.getEntityById(propertyId);
        return repository.findByPropertyId(propertyId).stream()
                .map(UnitResponse::from)
                .toList();
    }

    private Unit getEntityByIdAndProperty(UUID unitId, UUID propertyId) {
        Unit unit = repository.findById(unitId)
                .orElseThrow(() -> new UnitNotFoundException(unitId));

        // Une unite qui existe mais appartient a une AUTRE propriete est
        // traitee exactement comme une unite inexistante : on ne revele
        // jamais qu'elle existe ailleurs.
        if (!unit.getProperty().getId().equals(propertyId)) {
            throw new UnitNotFoundException(unitId);
        }

        return unit;
    }
}
