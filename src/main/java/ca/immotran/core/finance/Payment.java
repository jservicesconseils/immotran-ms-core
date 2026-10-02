package ca.immotran.core.finance;

import ca.immotran.core.lease.Lease;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Une echeance de loyer rattachee a un bail (cahier des charges, §8.9 :
 * "Echeancier, paiement, retard, recu et relance"). Le paiement peut
 * etre enregistre en plusieurs fois (partiel), voir recordPayment().
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "lease_id", nullable = false)
    private Lease lease;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "amount_due", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Payment() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Payment(Lease lease, LocalDate dueDate, BigDecimal amountDue) {
        this.lease = lease;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.amountPaid = BigDecimal.ZERO;
        this.status = PaymentStatus.DUE;
        this.createdAt = Instant.now();
    }

    /** Ajoute un montant encaisse (paiement complet ou partiel) et recalcule le statut. */
    public void recordPayment(BigDecimal amount, Instant paidAt) {
        this.amountPaid = this.amountPaid.add(amount);
        this.paidAt = paidAt;
        this.status = this.amountPaid.compareTo(this.amountDue) >= 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL;
    }

    public UUID getId() {
        return id;
    }

    public Lease getLease() {
        return lease;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
