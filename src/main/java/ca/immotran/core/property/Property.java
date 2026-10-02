package ca.immotran.core.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Property() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Property(UUID organizationId, PropertyType type, String street, String city, String province, String postalCode) {
        this.organizationId = organizationId;
        this.type = type;
        this.street = street;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
