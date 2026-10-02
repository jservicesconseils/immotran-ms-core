package ca.immotran.core.admin;

import ca.immotran.core.admin.dto.CreateJurisdictionRuleRequest;
import ca.immotran.core.admin.dto.JurisdictionRuleResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JurisdictionRuleService {

    private final JurisdictionRuleRepository repository;

    public JurisdictionRuleService(JurisdictionRuleRepository repository) {
        this.repository = repository;
    }

    public JurisdictionRuleResponse create(CreateJurisdictionRuleRequest request) {
        JurisdictionRule saved = repository.save(new JurisdictionRule(
                request.province(), request.ruleType(), request.description(),
                request.effectiveDate(), request.source(), request.version()));
        return JurisdictionRuleResponse.from(saved);
    }

    public JurisdictionRuleResponse getById(UUID id) {
        return JurisdictionRuleResponse.from(repository.findById(id)
                .orElseThrow(() -> new JurisdictionRuleNotFoundException(id)));
    }

    public List<JurisdictionRuleResponse> listByProvince(String province) {
        return repository.findByProvince(province).stream()
                .map(JurisdictionRuleResponse::from)
                .toList();
    }
}
