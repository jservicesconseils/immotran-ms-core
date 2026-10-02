package ca.immotran.core.finance.dto;

import ca.immotran.core.finance.Payment;
import ca.immotran.core.finance.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ce que l'API renvoie au client apres creation (ou lecture) d'une echeance. */
public record PaymentResponse(
        UUID id,
        UUID leaseId,
        LocalDate dueDate,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        Instant paidAt,
        PaymentStatus status,
        Instant createdAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getLease().getId(),
                payment.getDueDate(),
                payment.getAmountDue(),
                payment.getAmountPaid(),
                payment.getPaidAt(),
                payment.getStatus(),
                payment.getCreatedAt());
    }
}
