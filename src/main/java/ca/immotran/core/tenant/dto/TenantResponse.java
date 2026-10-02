package ca.immotran.core.tenant.dto;

import ca.immotran.core.tenant.Tenant;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'un locataire. */
public record TenantResponse(
        UUID id,
        UUID organizationId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Instant createdAt
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getOrganizationId(),
                tenant.getFirstName(),
                tenant.getLastName(),
                tenant.getEmail(),
                tenant.getPhone(),
                tenant.getCreatedAt());
    }
}
