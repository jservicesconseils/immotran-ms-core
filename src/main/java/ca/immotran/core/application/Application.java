package ca.immotran.core.application;

import ca.immotran.core.property.Unit;
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
import java.util.UUID;

/**
 * Une candidature de location soumise pour une unite (cahier des
 * charges : processus de "candidature" prealable a la signature du
 * bail). Soumise par un candidat NON authentifie -- voir
 * ApplicationController, endpoint public -- puis examinee par le
 * personnel de gestion (verification, evaluation, decision).
 *
 * Pas de champ organizationId direct, meme raisonnement que Lease :
 * le tenant (organisation) se deduit via unit.getProperty().getOrganizationId().
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "employer_name", length = 200)
    private String employerName;

    @Column(name = "monthly_income", precision = 10, scale = 2)
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    // Attribue par le personnel lors de l'evaluation (§ "solvabilite") --
    // pas de calcul automatique dans ce MVP, saisie manuelle.
    @Column(name = "solvency_score")
    private Integer solvencyScore;

    @Column(name = "review_comments", length = 2000)
    private String reviewComments;

    @Column(name = "decision_reason", length = 2000)
    private String decisionReason;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected Application() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Application(Unit unit, String firstName, String lastName, String email, String phone,
                        String employerName, BigDecimal monthlyIncome) {
        this.unit = unit;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.employerName = employerName;
        this.monthlyIncome = monthlyIncome;
        this.status = ApplicationStatus.EN_ATTENTE_VERIFICATION;
        this.submittedAt = Instant.now();
    }

    /** Passe la candidature en evaluation avec un score de solvabilite et des commentaires internes. */
    public void review(Integer solvencyScore, String reviewComments) {
        this.solvencyScore = solvencyScore;
        this.reviewComments = reviewComments;
        this.status = ApplicationStatus.EN_EVALUATION;
    }

    /** Met la candidature en attente d'un document ou d'une information manquante. */
    public void requestAdditionalInfo(String reviewComments) {
        this.reviewComments = reviewComments;
        this.status = ApplicationStatus.EN_ATTENTE_INFO;
    }

    /** Decision finale : accepte ou refuse la candidature. */
    public void decide(boolean accepted, String decisionReason) {
        this.status = accepted ? ApplicationStatus.ACCEPTEE : ApplicationStatus.REFUSEE;
        this.decisionReason = decisionReason;
        this.decidedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Unit getUnit() {
        return unit;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmployerName() {
        return employerName;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Integer getSolvencyScore() {
        return solvencyScore;
    }

    public String getReviewComments() {
        return reviewComments;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
