package ca.immotran.core.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Ce que le client envoie pour creer une regle juridictionnelle. */
public record CreateJurisdictionRuleRequest(

        @NotBlank(message = "la province est requise")
        @Size(min = 2, max = 2, message = "la province doit etre un code a 2 lettres (ex. QC, ON)")
        String province,

        @NotBlank(message = "le type de regle est requis")
        @Size(max = 100, message = "le type de regle ne doit pas depasser 100 caracteres")
        String ruleType,

        @NotBlank(message = "la description est requise")
        @Size(max = 1000, message = "la description ne doit pas depasser 1000 caracteres")
        String description,

        @NotNull(message = "la date d'effet est requise")
        LocalDate effectiveDate,

        @NotBlank(message = "la source est requise")
        @Size(max = 500, message = "la source ne doit pas depasser 500 caracteres")
        String source,

        @NotNull(message = "la version est requise")
        @Min(value = 1, message = "la version doit etre au moins 1")
        Integer version
) {
}
