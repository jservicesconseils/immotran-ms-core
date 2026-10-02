package ca.immotran.core.document;

import ca.immotran.core.document.dto.CreateDocumentRequest;
import ca.immotran.core.document.dto.DocumentResponse;
import ca.immotran.core.security.TenantClaims;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(request.organizationId());
        DocumentResponse created = documentService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/documents/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        DocumentResponse document = documentService.getById(id);
        TenantClaims.from(jwt).assertAccessTo(document.organizationId());
        return ResponseEntity.ok(document);
    }

    // organizationId est un parametre OBLIGATOIRE (pas seulement
    // entityType/entityId) : il sert a la fois au controle de tenant et
    // au filtre de la requete (defense en profondeur, voir DocumentRepository).
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listForEntity(@RequestParam UUID organizationId,
                                                                 @RequestParam String entityType,
                                                                 @RequestParam UUID entityId,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return ResponseEntity.ok(documentService.listForEntity(organizationId, entityType, entityId));
    }
}
