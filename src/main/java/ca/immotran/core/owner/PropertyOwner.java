package ca.immotran.core.owner;

import ca.immotran.core.property.Property;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Association N:N entre Property et Owner, avec la part de propriete
 * (cahier des charges, §11 "Parts copropriété"). Un meme couple
 * (propriete, proprietaire) ne peut exister qu'une seule fois -- on
 * modifierait la part existante plutot que d'en creer une deuxieme.
 */
@Entity
@Table(name = "property_owners", uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "owner_id"}))
public class PropertyOwner {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePercentage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected PropertyOwner() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public PropertyOwner(Property property, Owner owner, BigDecimal sharePercentage) {
        this.property = property;
        this.owner = owner;
        this.sharePercentage = sharePercentage;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Property getProperty() {
        return property;
    }

    public Owner getOwner() {
        return owner;
    }

    public BigDecimal getSharePercentage() {
        return sharePercentage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
