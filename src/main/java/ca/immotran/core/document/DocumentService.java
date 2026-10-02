package ca.immotran.core.document;

import ca.immotran.core.document.dto.CreateDocumentRequest;
import ca.immotran.core.document.dto.DocumentResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public DocumentResponse create(CreateDocumentRequest request) {
        Document saved = repository.save(new Document(
                request.organizationId(), request.entityType(), request.entityId(),
                request.type(), request.fileName(), request.s3Key()));
        return DocumentResponse.from(saved);
    }

    public DocumentResponse getById(UUID id) {
        return DocumentResponse.from(getEntityById(id));
    }

    public List<DocumentResponse> listForEntity(UUID organizationId, String entityType, UUID entityId) {
        return repository.findByOrganizationIdAndEntityTypeAndEntityId(organizationId, entityType, entityId).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    private Document getEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }
}
