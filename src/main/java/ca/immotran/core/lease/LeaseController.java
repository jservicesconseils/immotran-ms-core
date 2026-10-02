package ca.immotran.core.lease;

import ca.immotran.core.lease.dto.CreateLeaseRequest;
import ca.immotran.core.lease.dto.LeaseResponse;
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
@RequestMapping("/api/v1/properties/{propertyId}/units/{unitId}/leases")
public class LeaseController {

    private final LeaseService leaseService;
    private final PropertyService propertyService;

    public LeaseController(LeaseService leaseService, PropertyService propertyService) {
        this.leaseService = leaseService;
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<LeaseResponse> create(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                 @Valid @RequestBody CreateLeaseRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        LeaseResponse created = leaseService.create(propertyId, unitId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/units/" + unitId + "/leases/" + created.id()))
                .body(created);
    }

    @GetMapping("/{leaseId}")
    public ResponseEntity<LeaseResponse> getById(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                  @PathVariable UUID leaseId, @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(leaseService.getById(unitId, leaseId));
    }

    @GetMapping
    public ResponseEntity<List<LeaseResponse>> list(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(leaseService.listByUnit(propertyId, unitId));
    }
}
