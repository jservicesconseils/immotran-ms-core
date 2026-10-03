package ca.immotran.core.property.dto;

import ca.immotran.core.property.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Ce que le client envoie pour modifier une unite existante. Ne touche
 * pas a "principal" ni "status" -- voir Unit#update.
 */
public record UpdateUnitRequest(

        @NotBlank(message = "l'etiquette de l'unite est requise (ex. \"304\" ou \"Principal\")")
        @Size(max = 50, message = "l'etiquette ne doit pas depasser 50 caracteres")
        String label,

        Integer floor,

        @Min(value = 0, message = "la superficie ne peut pas etre negative")
        Double areaSquareMeters,

        @Min(value = 0, message = "le nombre de chambres ne peut pas etre negatif")
        Integer bedrooms,

        @Min(value = 0, message = "le nombre de salles de bain ne peut pas etre negatif")
        Integer bathrooms,

        UnitType type,

        @Size(max = 2000, message = "la description ne doit pas depasser 2000 caracteres")
        String description,

        @DecimalMin(value = "0.0", message = "le loyer affiche ne peut pas etre negatif")
        BigDecimal listedRent,

        @DecimalMin(value = "0.0", message = "le depot de garantie ne peut pas etre negatif")
        BigDecimal listedSecurityDeposit
) {
}
