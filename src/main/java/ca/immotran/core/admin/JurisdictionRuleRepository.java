package ca.immotran.core.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JurisdictionRuleRepository extends JpaRepository<JurisdictionRule, UUID> {

    List<JurisdictionRule> findByProvince(String province);
}
