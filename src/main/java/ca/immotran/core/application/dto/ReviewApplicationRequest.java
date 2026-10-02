package ca.immotran.core.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** Ce que le personnel de gestion envoie pour evaluer une candidature. */
public record ReviewApplicationRequest(

        @Min(value = 0, message = "le score de solvabilite doit etre entre 0 et 100")
        @Max(value = 100, message = "le score de solvabilite doit etre entre 0 et 100")
        Integer solvencyScore,

        @Size(max = 2000, message = "les commentaires ne doivent pas depasser 2000 caracteres")
        String reviewComments
) {
}
