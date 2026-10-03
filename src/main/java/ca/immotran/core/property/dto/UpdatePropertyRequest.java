package ca.immotran.core.property.dto;

import ca.immotran.core.property.BuildingStatus;
import ca.immotran.core.property.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Ce que le client envoie pour modifier une propriete existante. Memes
 * champs que CreatePropertyRequest, sans organizationId (le tenant d'une
 * propriete ne change jamais apres sa creation).
 */
public record UpdatePropertyRequest(

        @NotNull(message = "le type de propriete est requis")
        PropertyType type,

        @Size(max = 200, message = "le nom ne doit pas depasser 200 caracteres")
        String name,

        @NotBlank(message = "la rue est requise")
        @Size(max = 200, message = "la rue ne doit pas depasser 200 caracteres")
        String street,

        @NotBlank(message = "la ville est requise")
        @Size(max = 100, message = "la ville ne doit pas depasser 100 caracteres")
        String city,

        @NotBlank(message = "la province est requise")
        @Size(min = 2, max = 2, message = "la province doit etre un code a 2 lettres (ex. QC, ON)")
        String province,

        @NotBlank(message = "le code postal est requis")
        @Size(max = 10, message = "le code postal ne doit pas depasser 10 caracteres")
        String postalCode,

        @Size(max = 50, message = "le numero de cadastre ne doit pas depasser 50 caracteres")
        String cadastreNumber,

        @Size(max = 50, message = "le numero fiscal ne doit pas depasser 50 caracteres")
        String taxId,

        BuildingStatus buildingStatus,

        Integer yearBuilt,

        Integer floorCount,

        Double totalSurfaceArea,

        BigDecimal estimatedValue,

        @Size(max = 2000, message = "la description ne doit pas depasser 2000 caracteres")
        String description
) {
}
