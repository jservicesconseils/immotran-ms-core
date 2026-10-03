package ca.immotran.core.application;

import ca.immotran.core.application.dto.TenantApplicationStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoint PUBLIC separe (hors de /properties/{..}/units/{..}/applications,
 * voir ApplicationController) : le candidat consulte le statut de SA
 * candidature par le seul id -- son "numero de suivi" -- sans jeton et
 * sans connaitre propertyId/unitId. Voir SecurityConfig : permitAll sur
 * ce chemin precis, et TenantApplicationStatusResponse pour ce qui est
 * (volontairement) expose ou non.
 */
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationPublicStatusController {

    private final ApplicationService applicationService;

    public ApplicationPublicStatusController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{applicationId}/status")
    public ResponseEntity<TenantApplicationStatusResponse> getPublicStatus(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(applicationService.getPublicStatus(applicationId));
    }
}
