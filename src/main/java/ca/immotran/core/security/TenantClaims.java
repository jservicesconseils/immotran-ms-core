package ca.immotran.core.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

/**
 * Les claims de tenant portes par un jeton Cognito (voir immotran-ms-identity :
 * trigger Lambda "Pre Token Generation") : le tenant "principal" de
 * l'appelant (l'organisation -- proprietaire ou societe de gestion -- a
 * laquelle il appartient), et l'ensemble des tenants qu'il a le droit de
 * consulter (un seul pour la plupart des roles, plusieurs pour un
 * Gestionnaire externe administrant plusieurs proprietaires).
 *
 * Copie volontairement identique a la version d'immotran-ms-identity :
 * chaque microservice valide ce claim par lui-meme, sans jamais appeler
 * immotran-ms-identity par le reseau pour verifier un acces.
 *
 * Absence de {@code tenant_id} sur le jeton se traduit ici par un acces
 * refuse a toute ressource -- le login n'est jamais bloque, mais un jeton
 * sans tenant ne franchit aucune route scopee par tenant.
 */
public record TenantClaims(UUID tenantId, List<UUID> tenantScope) {

    private static final Logger log = LoggerFactory.getLogger(TenantClaims.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static TenantClaims from(Jwt jwt) {
        UUID tenantId = parseTenantId(jwt.getClaimAsString("tenant_id"), jwt.getSubject());

        if (tenantId == null) {
            return new TenantClaims(null, List.of());
        }

        return new TenantClaims(tenantId, parseTenantScope(jwt.getClaimAsString("tenant_scope"), tenantId));
    }

    public boolean hasAccessTo(UUID organizationId) {
        return tenantScope.contains(organizationId);
    }

    public void assertAccessTo(UUID organizationId) {
        if (!hasAccessTo(organizationId)) {
            throw new TenantAccessDeniedException(organizationId);
        }
    }

    private static UUID parseTenantId(String rawTenantId, String subject) {
        if (rawTenantId == null || rawTenantId.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(rawTenantId);
        } catch (IllegalArgumentException e) {
            log.warn("Claim tenant_id invalide (pas un UUID) sur le jeton de {} : {}", subject, rawTenantId);
            return null;
        }
    }

    // Sur une claim tenant_scope illisible ou absente, on retombe sur
    // [tenantId] plutot que de faire confiance a une valeur suspecte --
    // l'appelant garde acces a son propre tenant, jamais plus.
    private static List<UUID> parseTenantScope(String rawTenantScope, UUID tenantId) {
        if (rawTenantScope == null || rawTenantScope.isBlank()) {
            return List.of(tenantId);
        }

        try {
            List<String> ids = OBJECT_MAPPER.readValue(rawTenantScope, new TypeReference<List<String>>() { });
            List<UUID> tenantScope = ids.stream().map(UUID::fromString).toList();
            return tenantScope.isEmpty() ? List.of(tenantId) : tenantScope;
        } catch (Exception e) {
            log.warn("Claim tenant_scope illisible pour le tenant {}, acces restreint a ce seul tenant : {}", tenantId, rawTenantScope);
            return List.of(tenantId);
        }
    }
}
