package ca.immotran.core.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Ce que le client envoie pour creer un locataire. */
public record CreateTenantRequest(

        @NotNull(message = "l'organisation est requise")
        UUID organizationId,

        @NotBlank(message = "le prenom est requis")
        @Size(max = 100, message = "le prenom ne doit pas depasser 100 caracteres")
        String firstName,

        @NotBlank(message = "le nom est requis")
        @Size(max = 100, message = "le nom ne doit pas depasser 100 caracteres")
        String lastName,

        @Email(message = "le courriel n'est pas valide")
        @Size(max = 200, message = "le courriel ne doit pas depasser 200 caracteres")
        String email,

        @Size(max = 20, message = "le telephone ne doit pas depasser 20 caracteres")
        String phone
) {
}
