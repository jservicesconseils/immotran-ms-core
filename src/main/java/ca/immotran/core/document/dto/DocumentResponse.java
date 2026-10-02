package ca.immotran.core.document.dto;

import ca.immotran.core.document.Document;
import ca.immotran.core.document.DocumentType;

import java.time.Instant;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'un document. */
public record DocumentResponse(
        UUID id,
        UUID organizationId,
        String entityType,
        UUID entityId,
        DocumentType type,
        String fileName,
        String s3Key,
        Instant uploadedAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getOrganizationId(),
                document.getEntityType(),
                document.getEntityId(),
                document.getType(),
                document.getFileName(),
                document.getS3Key(),
                document.getUploadedAt());
    }
}
