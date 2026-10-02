package ca.immotran.core.owner;

import ca.immotran.core.owner.dto.AttachOwnerRequest;
import ca.immotran.core.owner.dto.CreateOwnerRequest;
import ca.immotran.core.owner.dto.OwnerResponse;
import ca.immotran.core.owner.dto.PropertyOwnerResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class OwnerController {

    private final OwnerService ownerService;
    private final PropertyService propertyService;

    public OwnerController(OwnerService ownerService, PropertyService propertyService) {
        this.ownerService = ownerService;
        this.propertyService = propertyService;
    }

    @PostMapping("/api/v1/owners")
    public ResponseEntity<OwnerResponse> create(@Valid @RequestBody CreateOwnerRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(request.organizationId());
        OwnerResponse created = ownerService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/owners/" + created.id()))
                .body(created);
    }

    @GetMapping("/api/v1/owners/{id}")
    public ResponseEntity<OwnerResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        OwnerResponse owner = ownerService.getById(id);
        TenantClaims.from(jwt).assertAccessTo(owner.organizationId());
        return ResponseEntity.ok(owner);
    }

    // Associe un proprietaire EXISTANT a une propriete existante, avec sa
    // part. Le tenant est verifie via l'organizationId de la propriete ;
    // OwnerService verifie en plus que le proprietaire appartient a la
    // meme organisation (defense en profondeur, voir OwnerService).
    @PostMapping("/api/v1/properties/{propertyId}/owners")
    public ResponseEntity<PropertyOwnerResponse> attachOwner(@PathVariable UUID propertyId,
                                                              @Valid @RequestBody AttachOwnerRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        PropertyOwnerResponse created = ownerService.attachToProperty(propertyId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/owners/" + created.id()))
                .body(created);
    }

    @GetMapping("/api/v1/properties/{propertyId}/owners")
    public ResponseEntity<List<PropertyOwnerResponse>> listForProperty(@PathVariable UUID propertyId,
                                                                        @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(ownerService.listForProperty(propertyId));
    }
}
