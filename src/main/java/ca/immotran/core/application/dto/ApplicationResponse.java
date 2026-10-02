package ca.immotran.core.application.dto;

import ca.immotran.core.application.Application;
import ca.immotran.core.application.ApplicationReference;
import ca.immotran.core.application.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Ce que l'API renvoie au client apres soumission (ou lecture) d'une candidature. */
public record ApplicationResponse(
        UUID id,
        UUID propertyId,
        UUID unitId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String employerName,
        BigDecimal monthlyIncome,
        ApplicationStatus status,
        Integer solvencyScore,
        String reviewComments,
        String decisionReason,
        List<ReferenceResponse> references,
        Instant submittedAt,
        Instant decidedAt
) {

    public static ApplicationResponse from(Application application, List<ApplicationReference> references) {
        return new ApplicationResponse(
                application.getId(),
                application.getUnit().getProperty().getId(),
                application.getUnit().getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getEmail(),
                application.getPhone(),
                application.getEmployerName(),
                application.getMonthlyIncome(),
                application.getStatus(),
                application.getSolvencyScore(),
                application.getReviewComments(),
                application.getDecisionReason(),
                references.stream().map(ReferenceResponse::from).toList(),
                application.getSubmittedAt(),
                application.getDecidedAt());
    }
}
