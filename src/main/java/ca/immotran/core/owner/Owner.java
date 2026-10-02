package ca.immotran.core.owner;

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
 * Un proprietaire (particulier ou societe) rattache a une organisation
 * (le tenant -- voir immotran-ms-identity). Un meme proprietaire peut
 * posseder plusieurs proprietes, et une propriete peut avoir plusieurs
 * proprietaires (voir PropertyOwner, l'association N:N avec parts).
 */
@Entity
@Table(name = "owners")
public class Owner {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OwnerType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Owner() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Owner(UUID organizationId, OwnerType type, String name, String email, String phone) {
        this.organizationId = organizationId;
        this.type = type;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public OwnerType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
