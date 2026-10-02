package ca.immotran.core.finance;

import ca.immotran.core.finance.dto.CreatePaymentRequest;
import ca.immotran.core.finance.dto.PaymentResponse;
import ca.immotran.core.finance.dto.RecordPaymentRequest;
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
@RequestMapping("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PropertyService propertyService;

    public PaymentController(PaymentService paymentService, PropertyService propertyService) {
        this.paymentService = paymentService;
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                   @PathVariable UUID leaseId, @Valid @RequestBody CreatePaymentRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        PaymentResponse created = paymentService.create(unitId, leaseId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/units/" + unitId
                        + "/leases/" + leaseId + "/payments/" + created.id()))
                .body(created);
    }

    @PostMapping("/{paymentId}/record")
    public ResponseEntity<PaymentResponse> recordPayment(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                          @PathVariable UUID leaseId, @PathVariable UUID paymentId,
                                                          @Valid @RequestBody RecordPaymentRequest request,
                                                          @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(paymentService.recordPayment(leaseId, paymentId, request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                    @PathVariable UUID leaseId, @PathVariable UUID paymentId,
                                                    @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(paymentService.getById(leaseId, paymentId));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> list(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                       @PathVariable UUID leaseId, @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(paymentService.listByLease(leaseId));
    }
}
