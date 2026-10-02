package ca.immotran.core.tenant;

import ca.immotran.core.tenant.dto.CreateTenantRequest;
import ca.immotran.core.tenant.dto.TenantResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository repository;

    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public TenantResponse create(CreateTenantRequest request) {
        Tenant saved = repository.save(new Tenant(
                request.organizationId(), request.firstName(), request.lastName(), request.email(), request.phone()));
        return TenantResponse.from(saved);
    }

    public TenantResponse getById(UUID id) {
        return TenantResponse.from(getEntityById(id));
    }

    public List<TenantResponse> listByOrganization(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(TenantResponse::from)
                .toList();
    }

    // Public : le module lease a besoin de l'entite brute pour ses
    // relations JPA (LeaseTenant), exactement comme PropertyService
    // l'expose pour owner/lease.
    public Tenant getEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
    }
}
