package ca.immotran.core.document.dto;

import ca.immotran.core.document.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Ce que le client envoie pour enregistrer les metadonnees d'un document
 * deja televerse sur S3 (le televersement lui-meme -- URL presignee --
 * est hors scope de ce recit, voir Document.java).
 */
public record CreateDocumentRequest(

        @NotNull(message = "l'organisation est requise")
        UUID organizationId,

        @NotBlank(message = "le type d'entite est requis (ex. PROPERTY, LEASE, TENANT)")
        @Size(max = 50, message = "le type d'entite ne doit pas depasser 50 caracteres")
        String entityType,

        @NotNull(message = "l'id de l'entite est requis")
        UUID entityId,

        @NotNull(message = "le type de document est requis")
        DocumentType type,

        @NotBlank(message = "le nom de fichier est requis")
        @Size(max = 255, message = "le nom de fichier ne doit pas depasser 255 caracteres")
        String fileName,

        @NotBlank(message = "la cle S3 est requise")
        @Size(max = 500, message = "la cle S3 ne doit pas depasser 500 caracteres")
        String s3Key
) {
}
