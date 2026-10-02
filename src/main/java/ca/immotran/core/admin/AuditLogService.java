package ca.immotran.core.admin;

import ca.immotran.core.admin.dto.AuditLogResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Voir le Javadoc d'AuditLog : record() est pret a etre appele depuis
 * les autres modules, mais aucun ne le fait encore automatiquement.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public AuditLogResponse record(UUID organizationId, String actorId, String action, String resourceType, UUID resourceId) {
        AuditLog saved = repository.save(new AuditLog(organizationId, actorId, action, resourceType, resourceId));
        return AuditLogResponse.from(saved);
    }

    public List<AuditLogResponse> listForOrganization(UUID organizationId) {
        return repository.findByOrganizationIdOrderByOccurredAtDesc(organizationId).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
