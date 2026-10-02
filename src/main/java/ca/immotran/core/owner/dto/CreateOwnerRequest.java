package ca.immotran.core.owner.dto;

import ca.immotran.core.owner.OwnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Ce que le client envoie pour creer un proprietaire. */
public record CreateOwnerRequest(

        @NotNull(message = "l'organisation est requise")
        UUID organizationId,

        @NotNull(message = "le type de proprietaire est requis")
        OwnerType type,

        @NotBlank(message = "le nom est requis")
        @Size(max = 200, message = "le nom ne doit pas depasser 200 caracteres")
        String name,

        @Email(message = "le courriel n'est pas valide")
        @Size(max = 200, message = "le courriel ne doit pas depasser 200 caracteres")
        String email,

        @Size(max = 20, message = "le telephone ne doit pas depasser 20 caracteres")
        String phone
) {
}
