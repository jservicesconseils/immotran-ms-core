package ca.immotran.core.dashboard.dto;

import ca.immotran.core.application.Application;
import ca.immotran.core.application.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

/** Ligne du tableau "Dossiers recents" du tableau de bord gestionnaire. */
public record RecentApplicationResponse(
        UUID id,
        UUID propertyId,
        UUID unitId,
        String firstName,
        String lastName,
        String unitLabel,
        ApplicationStatus status,
        Instant submittedAt
) {

    public static RecentApplicationResponse from(Application application) {
        return new RecentApplicationResponse(
                application.getId(),
                application.getUnit().getProperty().getId(),
                application.getUnit().getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getUnit().getLabel(),
                application.getStatus(),
                application.getSubmittedAt());
    }
}
