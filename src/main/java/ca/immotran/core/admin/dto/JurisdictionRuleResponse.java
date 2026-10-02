package ca.immotran.core.admin.dto;

import ca.immotran.core.admin.JurisdictionRule;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'une regle juridictionnelle. */
public record JurisdictionRuleResponse(
        UUID id,
        String province,
        String ruleType,
        String description,
        LocalDate effectiveDate,
        String source,
        Integer version,
        Instant createdAt
) {

    public static JurisdictionRuleResponse from(JurisdictionRule rule) {
        return new JurisdictionRuleResponse(
                rule.getId(),
                rule.getProvince(),
                rule.getRuleType(),
                rule.getDescription(),
                rule.getEffectiveDate(),
                rule.getSource(),
                rule.getVersion(),
                rule.getCreatedAt());
    }
}
