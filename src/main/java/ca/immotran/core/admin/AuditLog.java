package ca.immotran.core.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Une entree d'audit (cahier des charges, §14 : "AuditLog Acteur +
 * ressource + action + date"), rattachee a une organisation.
 *
 * ATTENTION : ce module fournit l'entite, le repository, le service
 * (record()) et la lecture, mais n'est PAS encore cable automatiquement
 * dans les autres modules (property, lease, etc.) -- aucun de leurs
 * services n'appelle AuditLogService.record() pour l'instant. Le cablage
 * complet (via un aspect Spring AOP sur les methodes mutatrices, par
 * exemple) est un sujet separe, volontairement non fait ici pour eviter
 * de modifier tous les controleurs deja ecrits et testes.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    // Le "sub" du jeton JWT (identifiant Cognito de l'utilisateur), pas
    // necessairement un UUID selon le fournisseur -- stocke en texte.
    @Column(name = "actor_id", nullable = false, length = 200)
    private String actorId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditLog() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public AuditLog(UUID organizationId, String actorId, String action, String resourceType, UUID resourceId) {
        this.organizationId = organizationId;
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.occurredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
