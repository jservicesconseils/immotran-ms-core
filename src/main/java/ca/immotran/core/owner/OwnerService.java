package ca.immotran.core.owner;

import ca.immotran.core.owner.dto.AttachOwnerRequest;
import ca.immotran.core.owner.dto.CreateOwnerRequest;
import ca.immotran.core.owner.dto.OwnerResponse;
import ca.immotran.core.owner.dto.PropertyOwnerResponse;
import ca.immotran.core.property.Property;
import ca.immotran.core.property.PropertyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * CRUD sur Owner, et gestion de son association N:N avec Property (parts
 * de propriete). Aucune logique de securite ici : le controleur verifie
 * le tenant_id via TenantClaims avant/apres appel.
 */
@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final PropertyOwnerRepository propertyOwnerRepository;
    private final PropertyService propertyService;

    public OwnerService(OwnerRepository ownerRepository, PropertyOwnerRepository propertyOwnerRepository,
                         PropertyService propertyService) {
        this.ownerRepository = ownerRepository;
        this.propertyOwnerRepository = propertyOwnerRepository;
        this.propertyService = propertyService;
    }

    public OwnerResponse create(CreateOwnerRequest request) {
        Owner saved = ownerRepository.save(new Owner(
                request.organizationId(), request.type(), request.name(), request.email(), request.phone()));
        return OwnerResponse.from(saved);
    }

    public OwnerResponse getById(UUID id) {
        return OwnerResponse.from(getEntityById(id));
    }

    public Owner getEntityById(UUID id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException(id));
    }

    public PropertyOwnerResponse attachToProperty(UUID propertyId, AttachOwnerRequest request) {
        Property property = propertyService.getEntityById(propertyId);
        Owner owner = getEntityById(request.ownerId());

        // Defense en profondeur : meme si l'appelant a acces aux deux
        // ressources, un proprietaire et une propriete d'organisations
        // differentes ne doivent jamais etre associes.
        if (!owner.getOrganizationId().equals(property.getOrganizationId())) {
            throw new OwnerOrganizationMismatchException(owner.getId(), propertyId);
        }

        if (propertyOwnerRepository.existsByPropertyIdAndOwnerId(propertyId, owner.getId())) {
            throw new OwnerAlreadyAttachedException(owner.getId(), propertyId);
        }

        PropertyOwner saved = propertyOwnerRepository.save(new PropertyOwner(property, owner, request.sharePercentage()));
        return PropertyOwnerResponse.from(saved);
    }

    public List<PropertyOwnerResponse> listForProperty(UUID propertyId) {
        // Valide au passage que la propriete existe (404 sinon).
        propertyService.getEntityById(propertyId);
        return propertyOwnerRepository.findByPropertyId(propertyId).stream()
                .map(PropertyOwnerResponse::from)
                .toList();
    }
}
