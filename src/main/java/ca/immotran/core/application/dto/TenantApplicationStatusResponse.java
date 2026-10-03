package ca.immotran.core.application.dto;

import ca.immotran.core.application.Application;
import ca.immotran.core.application.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Vue PUBLIQUE et minimale d'une candidature, destinee au candidat
 * lui-meme (espace locataire, voir ApplicationController.getPublicStatus).
 * Volontairement depourvue des champs internes au personnel de gestion
 * (solvencyScore, reviewComments) : le candidat ne doit voir ni son
 * score de solvabilite ni les notes internes.
 *
 * Accessible par id de candidature seul (UUID non devinable), sans
 * jeton -- meme principe qu'un numero de suivi de colis.
 */
public record TenantApplicationStatusResponse(
        UUID id,
        String firstName,
        String lastName,
        ApplicationStatus status,
        String unitLabel,
        String propertyStreet,
        String propertyCity,
        String propertyProvince,
        BigDecimal monthlyRent,
        Instant submittedAt,
        Instant decidedAt
) {

    public static TenantApplicationStatusResponse from(Application application) {
        return new TenantApplicationStatusResponse(
                application.getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getStatus(),
                application.getUnit().getLabel(),
                application.getUnit().getProperty().getStreet(),
                application.getUnit().getProperty().getCity(),
                application.getUnit().getProperty().getProvince(),
                application.getUnit().getListedRent(),
                application.getSubmittedAt(),
                application.getDecidedAt());
    }
}
