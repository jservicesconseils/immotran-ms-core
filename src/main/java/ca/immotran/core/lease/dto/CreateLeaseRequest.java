package ca.immotran.core.lease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ce que le client envoie pour creer un bail sur une unite. */
public record CreateLeaseRequest(

        @NotEmpty(message = "au moins un locataire est requis")
        List<UUID> tenantIds,

        @NotNull(message = "la date de debut est requise")
        LocalDate startDate,

        // Null = duree indeterminee (reconduction tacite) -- pas de contrainte.
        LocalDate endDate,

        @NotNull(message = "le loyer mensuel est requis")
        @DecimalMin(value = "0.0", inclusive = false, message = "le loyer mensuel doit etre positif")
        BigDecimal monthlyRent
) {
}
