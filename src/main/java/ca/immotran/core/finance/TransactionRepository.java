package ca.immotran.core.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByPropertyId(UUID propertyId);

    // Utilise par le dashboard (§20 "Revenus attendus/encaisses", "Depenses").
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.property.organizationId = :organizationId AND t.type = :type
            """)
    BigDecimal sumAmountByOrganizationIdAndType(UUID organizationId, TransactionType type);
}
