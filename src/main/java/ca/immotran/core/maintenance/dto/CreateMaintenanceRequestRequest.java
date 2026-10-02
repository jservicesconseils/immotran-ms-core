package ca.immotran.core.maintenance.dto;

import ca.immotran.core.maintenance.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Ce que le client envoie pour creer une demande de maintenance. */
public record CreateMaintenanceRequestRequest(

        // Null = la demande concerne la propriete entiere / un espace
        // commun, pas une unite en particulier.
        UUID unitId,

        @NotBlank(message = "la description est requise")
        @Size(max = 1000, message = "la description ne doit pas depasser 1000 caracteres")
        String description,

        @NotNull(message = "la priorite est requise")
        MaintenancePriority priority
) {
}
