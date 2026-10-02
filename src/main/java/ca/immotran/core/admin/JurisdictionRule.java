package ca.immotran.core.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Une regle juridictionnelle (cahier des charges, §7) : preavis, hausse
 * de loyer, delai de renouvellement, etc. Donnee de REFERENCE PARTAGEE
 * par toute la plateforme -- PAS une donnee d'organisation, donc pas de
 * champ organizationId et pas de verification de tenant_id sur ses
 * routes (voir JurisdictionRuleController).
 *
 * ruleType est une chaine libre (pas un enum) : le catalogue exact n'est
 * pas fige au MVP (§7 : "Le systeme ne doit pas coder les regles
 * locatives dans le coeur de l'application").
 */
@Entity
@Table(name = "jurisdiction_rules")
public class JurisdictionRule {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 2)
    private String province;

    @Column(name = "rule_type", nullable = false, length = 100)
    private String ruleType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false, length = 500)
    private String source;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected JurisdictionRule() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public JurisdictionRule(String province, String ruleType, String description,
                             LocalDate effectiveDate, String source, Integer version) {
        this.province = province;
        this.ruleType = ruleType;
        this.description = description;
        this.effectiveDate = effectiveDate;
        this.source = source;
        this.version = version;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getProvince() {
        return province;
    }

    public String getRuleType() {
        return ruleType;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public String getSource() {
        return source;
    }

    public Integer getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
