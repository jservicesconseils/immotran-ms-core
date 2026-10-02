package ca.immotran.core.property;

import ca.immotran.core.property.dto.CreatePropertyRequest;
import ca.immotran.core.property.dto.CreateUnitRequest;
import ca.immotran.core.property.dto.PropertyResponse;
import ca.immotran.core.property.dto.UnitResponse;
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
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UnitService unitService;

    public PropertyController(PropertyService propertyService, UnitService unitService) {
        this.propertyService = propertyService;
        this.unitService = unitService;
    }

    // Le tenant_id vient du corps de la requete (organizationId), pas du
    // jeton seul : on verifie que l'appelant a bien le droit de creer une
    // propriete pour CETTE organisation avant de toucher la base.
    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody CreatePropertyRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(request.organizationId());
        PropertyResponse created = propertyService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + created.id()))
                .body(created);
    }

    // Contrairement a Organization (ou id == tenant_id), ici l'id de la
    // ressource et le tenant sont deux champs distincts : il faut donc
    // d'abord charger la propriete (404 si elle n'existe pas) pour
    // connaitre son organizationId, puis verifier l'acces (403 sinon).
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(id);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(property);
    }

    @PostMapping("/{propertyId}/units")
    public ResponseEntity<UnitResponse> createUnit(@PathVariable UUID propertyId,
                                                    @Valid @RequestBody CreateUnitRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        UnitResponse created = unitService.create(propertyId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/units/" + created.id()))
                .body(created);
    }

    @GetMapping("/{propertyId}/units/{unitId}")
    public ResponseEntity<UnitResponse> getUnitById(@PathVariable UUID propertyId,
                                                     @PathVariable UUID unitId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(unitService.getById(propertyId, unitId));
    }

    @GetMapping("/{propertyId}/units")
    public ResponseEntity<List<UnitResponse>> listUnits(@PathVariable UUID propertyId,
                                                         @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(unitService.listByProperty(propertyId));
    }
}
