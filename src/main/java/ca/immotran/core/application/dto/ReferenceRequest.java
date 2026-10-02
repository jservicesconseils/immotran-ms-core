package ca.immotran.core.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Une reference fournie dans une candidature (SubmitApplicationRequest). */
public record ReferenceRequest(

        @NotBlank(message = "le nom de la reference est requis")
        @Size(max = 200, message = "le nom ne doit pas depasser 200 caracteres")
        String name,

        @Size(max = 30, message = "le telephone ne doit pas depasser 30 caracteres")
        String phone,

        @Size(max = 200, message = "le courriel ne doit pas depasser 200 caracteres")
        String email
) {
}
