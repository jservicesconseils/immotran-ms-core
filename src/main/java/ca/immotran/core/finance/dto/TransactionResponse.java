package ca.immotran.core.finance.dto;

import ca.immotran.core.finance.Transaction;
import ca.immotran.core.finance.TransactionCategory;
import ca.immotran.core.finance.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'une transaction. */
public record TransactionResponse(
        UUID id,
        UUID propertyId,
        TransactionType type,
        TransactionCategory category,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        Instant createdAt
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getProperty().getId(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt());
    }
}
