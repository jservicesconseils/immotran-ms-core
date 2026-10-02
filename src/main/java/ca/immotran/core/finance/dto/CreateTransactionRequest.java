package ca.immotran.core.finance.dto;

import ca.immotran.core.finance.TransactionCategory;
import ca.immotran.core.finance.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ce que le client envoie pour enregistrer un revenu ou une depense sur une propriete. */
public record CreateTransactionRequest(

        @NotNull(message = "le type (revenu/depense) est requis")
        TransactionType type,

        @NotNull(message = "la categorie est requise")
        TransactionCategory category,

        @NotNull(message = "le montant est requis")
        @DecimalMin(value = "0.0", inclusive = false, message = "le montant doit etre positif")
        BigDecimal amount,

        @Size(max = 500, message = "la description ne doit pas depasser 500 caracteres")
        String description,

        @NotNull(message = "la date de transaction est requise")
        LocalDate transactionDate
) {
}
