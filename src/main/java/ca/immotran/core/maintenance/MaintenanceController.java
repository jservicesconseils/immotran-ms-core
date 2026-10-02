package ca.immotran.core.maintenance;

import ca.immotran.core.maintenance.dto.CloseMaintenanceRequestRequest;
import ca.immotran.core.maintenance.dto.CreateMaintenanceRequestRequest;
import ca.immotran.core.maintenance.dto.MaintenanceRequestResponse;
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
@RequestMapping("/api/v1/properties/{propertyId}/maintenance-requests")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final PropertyService propertyService;

    public MaintenanceController(MaintenanceService maintenanceService, PropertyService propertyService) {
        this.maintenanceService = maintenanceService;
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequestResponse> create(@PathVariable UUID propertyId,
                                                              @Valid @RequestBody CreateMaintenanceRequestRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        MaintenanceRequestResponse created = maintenanceService.create(propertyId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/maintenance-requests/" + created.id()))
                .body(created);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<MaintenanceRequestResponse> close(@PathVariable UUID propertyId, @PathVariable UUID id,
                                                             @Valid @RequestBody CloseMaintenanceRequestRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(maintenanceService.close(propertyId, id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponse> getById(@PathVariable UUID propertyId, @PathVariable UUID id,
                                                               @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(maintenanceService.getById(propertyId, id));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceRequestResponse>> list(@PathVariable UUID propertyId,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(maintenanceService.listByProperty(propertyId));
    }
}
