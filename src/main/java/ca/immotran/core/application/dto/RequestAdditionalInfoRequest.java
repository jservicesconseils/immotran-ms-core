package ca.immotran.core.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ce que le personnel de gestion envoie pour demander un document ou une information manquante. */
public record RequestAdditionalInfoRequest(

        @NotBlank(message = "les commentaires sont requis")
        @Size(max = 2000, message = "les commentaires ne doivent pas depasser 2000 caracteres")
        String reviewComments
) {
}
