package ca.immotran.core.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Un actif immobilier physique (maison, condo, duplex/triplex/quadruplex
 * ou immeuble multi-unites), rattache a une organisation (le tenant :
 * proprietaire ou societe de gestion -- voir immotran-ms-identity).
 *
 * Suit le modele du cahier des charges (§14) : Property 1→N Unit. Une
 * maison individuelle est une Property avec UNE SEULE Unit "principale" ;
 * un immeuble est une Property avec N Unit. Cette uniformite evite de
 * devoir "creer artificiellement plusieurs unites" pour une maison tout
 * en gerant les immeubles multi-unites avec le meme modele.
 *
 * organizationId n'est PAS une relation JPA vers une table Organization :
 * cette donnee vit dans immotran-ms-identity, un autre service/base. On
 * stocke uniquement l'identifiant du tenant, comme decrit dans son README.
 */
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PropertyType type;

    // Nom usuel de la propriete (ex. "Le Domaine du Parc"), distinct de son
    // adresse -- purement cosmetique, affiche dans la liste/fiche quand
    // fourni, jamais utilise comme identifiant.
    @Column(length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    // Code a 2 lettres (QC, ON, BC, ...) -- la juridiction du bien. Le
    // catalogue complet de regles par province/territoire (§7 du cahier
    // des charges) est hors scope du MVP ; on se contente ici d'enregistrer
    // la juridiction, critere d'acceptation explicite du MVP (§22).
    @Column(nullable = false, length = 2)
    private String province;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyStatus status;

    // Champs legaux/financiers optionnels -- tous nullable : la plupart des
    // utilisateurs ne les connaitront pas des la creation d'une propriete,
    // et on ne veut pas bloquer la creation pour une donnee qu'on pourra
    // completer plus tard (pas d'endpoint de mise a jour pour l'instant,
    // hors scope de ce MVP, voir README).
    @Column(name = "cadastre_number", length = 50)
    private String cadastreNumber;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_status", length = 20)
    private BuildingStatus buildingStatus;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "floor_count")
    private Integer floorCount;

    @Column(name = "total_surface_area")
    private Double totalSurfaceArea;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Property() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Property(UUID organizationId, PropertyType type, String name, String street, String city, String province, String postalCode,
                     String cadastreNumber, String taxId, BuildingStatus buildingStatus, Integer yearBuilt,
                     Integer floorCount, Double totalSurfaceArea, BigDecimal estimatedValue, String description) {
        this.organizationId = organizationId;
        this.type = type;
        this.name = name;
        this.street = street;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.cadastreNumber = cadastreNumber;
        this.taxId = taxId;
        this.buildingStatus = buildingStatus;
        this.yearBuilt = yearBuilt;
        this.floorCount = floorCount;
        this.totalSurfaceArea = totalSurfaceArea;
        this.estimatedValue = estimatedValue;
        this.description = description;
        this.status = PropertyStatus.VACANTE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public PropertyType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public String getCadastreNumber() {
        return cadastreNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    public BuildingStatus getBuildingStatus() {
        return buildingStatus;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public Integer getFloorCount() {
        return floorCount;
    }

    public Double getTotalSurfaceArea() {
        return totalSurfaceArea;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Mutateurs ajoutes pour l'edition d'une propriete (ecran "Modifier la
    // propriete" de la maquette) -- la creation initiale continue de passer
    // par le constructeur, ces setters ne servent qu'a PropertyService#update.
    public void update(PropertyType type, String name, String street, String city, String province, String postalCode,
                        String cadastreNumber, String taxId, BuildingStatus buildingStatus, Integer yearBuilt,
                        Integer floorCount, Double totalSurfaceArea, BigDecimal estimatedValue, String description) {
        this.type = type;
        this.name = name;
        this.street = street;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.cadastreNumber = cadastreNumber;
        this.taxId = taxId;
        this.buildingStatus = buildingStatus;
        this.yearBuilt = yearBuilt;
        this.floorCount = floorCount;
        this.totalSurfaceArea = totalSurfaceArea;
        this.estimatedValue = estimatedValue;
        this.description = description;
    }
}
