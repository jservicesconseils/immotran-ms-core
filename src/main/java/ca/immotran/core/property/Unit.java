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

import java.math.BigDecimal;
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
    @Column(length = 30)
    private UnitType type;

    @Column(length = 2000)
    private String description;

    // Loyer affiche pour la mise en location (annonce), independant de
    // tout bail : existe avant meme qu'un candidat postule (ecran
    // "Location souhaitee" du depot de dossier). Le loyer reel d'un
    // bail signe est stocke separement sur Lease.monthlyRent.
    @Column(name = "listed_rent", precision = 10, scale = 2)
    private BigDecimal listedRent;

    // Depot de garantie affiche pour la mise en location (annonce), au
    // meme titre que listedRent -- distinct du depot reellement percu sur
    // un bail signe (Lease.securityDeposit).
    @Column(name = "listed_security_deposit", precision = 10, scale = 2)
    private BigDecimal listedSecurityDeposit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Unit() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Unit(Property property, String label, boolean principal, Integer floor,
                Double areaSquareMeters, Integer bedrooms, Integer bathrooms,
                UnitType type, String description, BigDecimal listedRent,
                BigDecimal listedSecurityDeposit, UnitStatus status) {
        this.property = property;
        this.label = label;
        this.principal = principal;
        this.floor = floor;
        this.areaSquareMeters = areaSquareMeters;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.type = type;
        this.description = description;
        this.listedRent = listedRent;
        this.listedSecurityDeposit = listedSecurityDeposit;
        this.status = status != null ? status : UnitStatus.DISPONIBLE;
        this.createdAt = Instant.now();
    }

    // Mutateur ajoute pour l'edition d'une unite (ecran "Modifier
    // l'appartement" de la maquette) ; ne touche pas au statut, qui suit
    // son propre cycle (markOccupied, baux) plutot que d'etre reecrit en
    // bloc depuis un formulaire.
    public void update(String label, Integer floor, Double areaSquareMeters, Integer bedrooms, Integer bathrooms,
                        UnitType type, String description, BigDecimal listedRent, BigDecimal listedSecurityDeposit) {
        this.label = label;
        this.floor = floor;
        this.areaSquareMeters = areaSquareMeters;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.type = type;
        this.description = description;
        this.listedRent = listedRent;
        this.listedSecurityDeposit = listedSecurityDeposit;
    }

    /** Appele a la signature d'un bail (cahier des charges, §4 "prise de possession"). */
    public void markOccupied() {
        this.status = UnitStatus.OCCUPEE;
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

    public UnitType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getListedRent() {
        return listedRent;
    }

    public BigDecimal getListedSecurityDeposit() {
        return listedSecurityDeposit;
    }

    public UnitStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
