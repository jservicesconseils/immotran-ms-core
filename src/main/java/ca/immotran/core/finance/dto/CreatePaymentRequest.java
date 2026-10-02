package ca.immotran.core.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ce que le client envoie pour creer une echeance de loyer sur un bail. */
public record CreatePaymentRequest(

        @NotNull(message = "la date d'echeance est requise")
        LocalDate dueDate,

        @NotNull(message = "le montant du is requis")
        @DecimalMin(value = "0.0", inclusive = false, message = "le montant du doit etre positif")
        BigDecimal amountDue
) {
}
