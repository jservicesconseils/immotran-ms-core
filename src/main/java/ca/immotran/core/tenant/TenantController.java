package ca.immotran.core.tenant;

import ca.immotran.core.security.TenantClaims;
import ca.immotran.core.tenant.dto.CreateTenantRequest;
import ca.immotran.core.tenant.dto.TenantResponse;
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
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(request.organizationId());
        TenantResponse created = tenantService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/tenants/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        TenantResponse tenant = tenantService.getById(id);
        TenantClaims.from(jwt).assertAccessTo(tenant.organizationId());
        return ResponseEntity.ok(tenant);
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> listByOrganization(@RequestParam UUID organizationId,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        TenantClaims.from(jwt).assertAccessTo(organizationId);
        return ResponseEntity.ok(tenantService.listByOrganization(organizationId));
    }
}
