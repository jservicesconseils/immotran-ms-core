package ca.immotran.core.finance;

import ca.immotran.core.finance.dto.CreateTransactionRequest;
import ca.immotran.core.finance.dto.TransactionResponse;
import ca.immotran.core.property.PropertyService;
import ca.immotran.core.property.dto.PropertyResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final PropertyService propertyService;

    public TransactionController(TransactionService transactionService, PropertyService propertyService) {
        this.transactionService = transactionService;
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@PathVariable UUID propertyId,
                                                       @Valid @RequestBody CreateTransactionRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        TransactionResponse created = transactionService.create(propertyId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/transactions/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID propertyId, @PathVariable UUID id,
                                                        @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(transactionService.getById(propertyId, id));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(@PathVariable UUID propertyId,
                                                           @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(transactionService.listByProperty(propertyId));
    }
}
