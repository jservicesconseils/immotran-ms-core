package ca.immotran.core.dashboard;

import ca.immotran.core.dashboard.dto.DashboardResponse;
import ca.immotran.core.security.TenantClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Comme Organization dans immotran-ms-identity : l'id du chemin EST
    // le tenant_id, donc on verifie l'acces AVANT de toucher la base
    // (403 direct, sans jamais interroger les repositories).
    @GetMapping
    public ResponseEntity<DashboardResponse> get(@PathVariable UUID organizationId, @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return ResponseEntity.ok(dashboardService.getForOrganization(organizationId));
    }
}
