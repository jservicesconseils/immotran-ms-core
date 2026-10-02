package ca.immotran.core.owner.dto;

import ca.immotran.core.owner.Owner;
import ca.immotran.core.owner.OwnerType;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'un proprietaire. */
public record OwnerResponse(
        UUID id,
        UUID organizationId,
        OwnerType type,
        String name,
        String email,
        String phone,
        Instant createdAt
) {

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(
                owner.getId(),
                owner.getOrganizationId(),
                owner.getType(),
                owner.getName(),
                owner.getEmail(),
                owner.getPhone(),
                owner.getCreatedAt());
    }
}
