package ca.immotran.core.admin.dto;

import ca.immotran.core.admin.AuditLog;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie pour une entree du journal d'audit. */
public record AuditLogResponse(
        UUID id,
        UUID organizationId,
        String actorId,
        String action,
        String resourceType,
        UUID resourceId,
        Instant occurredAt
) {

    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getOrganizationId(),
                auditLog.getActorId(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getOccurredAt());
    }
}
