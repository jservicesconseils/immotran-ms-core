package ca.immotran.core.admin;

import ca.immotran.core.admin.dto.CreateJurisdictionRuleRequest;
import ca.immotran.core.admin.dto.JurisdictionRuleResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

/**
 * Catalogue de regles juridictionnelles (§7) : donnee de reference
 * PARTAGEE par toute la plateforme, pas une donnee d'organisation --
 * aucune verification de tenant_id ici (TenantClaims ne s'applique pas),
 * seulement l'authentification standard (voir SecurityConfig).
 *
 * Les roles ne sont pas encore modelises dans ce service (voir
 * immotran-ms-identity) : en attendant un vrai RBAC, N'IMPORTE QUEL
 * utilisateur authentifie peut creer une regle. A restreindre aux
 * super-administrateurs une fois les roles disponibles dans le jeton.
 */
@RestController
@RequestMapping("/api/v1/admin/jurisdiction-rules")
public class JurisdictionRuleController {

    private final JurisdictionRuleService service;

    public JurisdictionRuleController(JurisdictionRuleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JurisdictionRuleResponse> create(@Valid @RequestBody CreateJurisdictionRuleRequest request) {
        JurisdictionRuleResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/admin/jurisdiction-rules/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JurisdictionRuleResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<JurisdictionRuleResponse>> listByProvince(@RequestParam String province) {
        return ResponseEntity.ok(service.listByProvince(province));
    }
}
