package ca.immotran.core.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ce qu'un candidat (NON authentifie, voir ApplicationController) envoie
 * pour soumettre une candidature sur une unite.
 */
public record SubmitApplicationRequest(

        @NotBlank(message = "le prenom est requis")
        @Size(max = 100, message = "le prenom ne doit pas depasser 100 caracteres")
        String firstName,

        @NotBlank(message = "le nom est requis")
        @Size(max = 100, message = "le nom ne doit pas depasser 100 caracteres")
        String lastName,

        @NotBlank(message = "le courriel est requis")
        @Email(message = "le courriel doit etre valide")
        @Size(max = 200, message = "le courriel ne doit pas depasser 200 caracteres")
        String email,

        @NotBlank(message = "le telephone est requis")
        @Size(max = 30, message = "le telephone ne doit pas depasser 30 caracteres")
        String phone,

        @Size(max = 200, message = "le nom de l'employeur ne doit pas depasser 200 caracteres")
        String employerName,

        @DecimalMin(value = "0.0", message = "le revenu mensuel ne peut pas etre negatif")
        BigDecimal monthlyIncome,

        @NotEmpty(message = "au moins une reference est requise")
        List<@Valid ReferenceRequest> references
) {
}
