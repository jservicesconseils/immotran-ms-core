package ca.immotran.core.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Un logement louable individualise, rattache a une Property. Pour une
 * maison individuelle, il existe exactement une Unit avec isPrincipal=true ;
 * pour un immeuble, plusieurs Unit independantes (voir Property).
 */
@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    // Numero/etiquette du logement (ex. "304", "Principal") -- pas de
    // contrainte de format, chaque type de propriete a sa propre convention.
    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "is_principal", nullable = false)
    private boolean principal;

    private Integer floor;

    @Column(name = "area_square_meters")
    private Double areaSquareMeters;

    private Integer bedrooms;

    private Integer bathrooms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Unit() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Unit(Property property, String label, boolean principal, Integer floor,
                Double areaSquareMeters, Integer bedrooms, Integer bathrooms) {
        this.property = property;
        this.label = label;
        this.principal = principal;
        this.floor = floor;
        this.areaSquareMeters = areaSquareMeters;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.status = UnitStatus.DISPONIBLE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Property getProperty() {
        return property;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public Integer getFloor() {
        return floor;
    }

    public Double getAreaSquareMeters() {
        return areaSquareMeters;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public UnitStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
