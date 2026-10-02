package ca.immotran.core.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Ce que le personnel de gestion envoie pour accepter ou refuser une candidature. */
public record DecideApplicationRequest(

        @NotNull(message = "la decision est requise")
        Boolean accepted,

        @Size(max = 2000, message = "le motif ne doit pas depasser 2000 caracteres")
        String decisionReason
) {
}
