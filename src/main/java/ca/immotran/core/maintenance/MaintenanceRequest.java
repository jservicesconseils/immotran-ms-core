package ca.immotran.core.maintenance;

import ca.immotran.core.property.Property;
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
 * Une demande de maintenance, au niveau propriete, unite, ou espace
 * commun (cahier des charges, §12 : "unit" nullable couvre les trois cas
 * -- unit=null signifie "propriete entiere / espace commun").
 */
@Entity
@Table(name = "maintenance_requests")
public class MaintenanceRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenancePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceStatus status;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected MaintenanceRequest() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public MaintenanceRequest(Property property, Unit unit, String description, MaintenancePriority priority) {
        this.property = property;
        this.unit = unit;
        this.description = description;
        this.priority = priority;
        this.status = MaintenanceStatus.OUVERTE;
        this.createdAt = Instant.now();
    }

    /** Cloture la demande : enregistre le prestataire et le cout, passe le statut a TERMINEE. */
    public void close(String vendorName, BigDecimal cost) {
        this.vendorName = vendorName;
        this.cost = cost;
        this.status = MaintenanceStatus.TERMINEE;
        this.closedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Property getProperty() {
        return property;
    }

    public Unit getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    public MaintenancePriority getPriority() {
        return priority;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public String getVendorName() {
        return vendorName;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}
