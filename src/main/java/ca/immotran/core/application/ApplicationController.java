package ca.immotran.core.application;

import ca.immotran.core.application.dto.ApplicationResponse;
import ca.immotran.core.application.dto.DecideApplicationRequest;
import ca.immotran.core.application.dto.RequestAdditionalInfoRequest;
import ca.immotran.core.application.dto.ReviewApplicationRequest;
import ca.immotran.core.application.dto.SubmitApplicationRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Endpoint de soumission (POST) volontairement PUBLIC (voir
 * SecurityConfig : permitAll sur ce chemin precis) -- un candidat a la
 * location n'a pas de compte et ne doit pas en creer un pour postuler.
 * Toutes les autres actions (lecture, evaluation, decision) restent
 * reservees au personnel de gestion, authentifie et verifie via
 * TenantClaims comme partout ailleurs dans le monolithe.
 */
@RestController
@RequestMapping("/api/v1/properties/{propertyId}/units/{unitId}/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final PropertyService propertyService;

    public ApplicationController(ApplicationService applicationService, PropertyService propertyService) {
        this.applicationService = applicationService;
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> submit(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                       @Valid @RequestBody SubmitApplicationRequest request) {
        ApplicationResponse created = applicationService.submit(propertyId, unitId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/properties/" + propertyId + "/units/" + unitId + "/applications/" + created.id()))
                .body(created);
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                        @PathVariable UUID applicationId, @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(applicationService.getById(propertyId, unitId, applicationId));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> list(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                           @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(applicationService.listByUnit(propertyId, unitId));
    }

    @PutMapping("/{applicationId}/evaluation")
    public ResponseEntity<ApplicationResponse> review(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                       @PathVariable UUID applicationId,
                                                       @Valid @RequestBody ReviewApplicationRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(applicationService.review(propertyId, unitId, applicationId, request));
    }

    @PutMapping("/{applicationId}/demande-information")
    public ResponseEntity<ApplicationResponse> requestAdditionalInfo(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                                      @PathVariable UUID applicationId,
                                                                      @Valid @RequestBody RequestAdditionalInfoRequest request,
                                                                      @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(applicationService.requestAdditionalInfo(propertyId, unitId, applicationId, request));
    }

    @PutMapping("/{applicationId}/decision")
    public ResponseEntity<ApplicationResponse> decide(@PathVariable UUID propertyId, @PathVariable UUID unitId,
                                                       @PathVariable UUID applicationId,
                                                       @Valid @RequestBody DecideApplicationRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        PropertyResponse property = propertyService.getById(propertyId);
        TenantClaims.from(jwt).assertAccessTo(property.organizationId());
        return ResponseEntity.ok(applicationService.decide(propertyId, unitId, applicationId, request));
    }
}
