package ca.immotran.core.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    // organizationId fait partie du filtre (pas seulement entityType +
    // entityId) : defense en profondeur, pour ne jamais renvoyer un
    // document d'une autre organisation meme en cas de collision d'id.
    List<Document> findByOrganizationIdAndEntityTypeAndEntityId(UUID organizationId, String entityType, UUID entityId);
}
