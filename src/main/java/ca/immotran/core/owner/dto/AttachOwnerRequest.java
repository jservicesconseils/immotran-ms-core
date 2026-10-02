package ca.immotran.core.owner.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/** Ce que le client envoie pour associer un proprietaire existant a une propriete. */
public record AttachOwnerRequest(

        @NotNull(message = "le proprietaire est requis")
        UUID ownerId,

        @NotNull(message = "la part de propriete est requise")
        @DecimalMin(value = "0.0", message = "la part de propriete ne peut pas etre negative")
        @DecimalMax(value = "100.0", message = "la part de propriete ne peut pas depasser 100")
        BigDecimal sharePercentage
) {
}
