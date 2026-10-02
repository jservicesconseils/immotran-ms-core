package ca.immotran.core.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadonnees d'un document (cahier des charges, §8.12/§13). Le fichier
 * lui-meme vit dans S3 (voir §16 architecture AWS) ; ce service ne stocke
 * que la reference (s3Key) et les metadonnees -- l'upload via URL
 * presignee (US-100 du backlog) est un sujet infra separe, hors scope
 * de ce premier recit.
 *
 * Reference polymorphe volontaire (entityType + entityId) plutot qu'une
 * relation JPA directe : un document peut s'attacher a une Property, un
 * Lease, un Tenant, un Owner, etc. -- autant de types que de modules.
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType type;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    protected Document() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Document(UUID organizationId, String entityType, UUID entityId, DocumentType type, String fileName, String s3Key) {
        this.organizationId = organizationId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.type = type;
        this.fileName = fileName;
        this.s3Key = s3Key;
        this.uploadedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public DocumentType getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public String getS3Key() {
        return s3Key;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
