package ca.immotran.core.lease;

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
import java.time.LocalDate;
import java.util.UUID;

/**
 * Un bail : Unit + Tenant(s) + dates + loyer (cahier des charges, §10/§14).
 * Pas de champ organizationId direct : le tenant (organisation) se deduit
 * transitivement via unit.getProperty().getOrganizationId(), exactement
 * comme pour Unit lui-meme -- un bail n'existe jamais hors du contexte
 * d'une unite.
 *
 * La juridiction du bail (§10 : "Bail rattache a une unite et a une
 * juridiction") est celle de la propriete (Property.province) ; elle
 * n'est pas dupliquee ici.
 */
@Entity
@Table(name = "leases")
public class Lease {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Null = bail a duree indeterminee (ex. reconduction tacite). Un bail
    // a date fixe aura une valeur ici.
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaseStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Lease() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Lease(Unit unit, LocalDate startDate, LocalDate endDate, BigDecimal monthlyRent) {
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.status = LeaseStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Unit getUnit() {
        return unit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public LeaseStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
