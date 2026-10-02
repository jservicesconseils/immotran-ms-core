package ca.immotran.core.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/** Ce que le client envoie pour enregistrer un paiement (complet ou partiel) sur une echeance. */
public record RecordPaymentRequest(

        @NotNull(message = "le montant encaisse est requis")
        @DecimalMin(value = "0.0", inclusive = false, message = "le montant encaisse doit etre positif")
        BigDecimal amountPaid,

        // Null = on utilise l'heure courante.
        Instant paidAt
) {
}
