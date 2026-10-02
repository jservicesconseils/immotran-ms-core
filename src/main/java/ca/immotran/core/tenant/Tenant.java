package ca.immotran.core.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Un locataire, rattache a une organisation (le tenant SaaS -- voir
 * immotran-ms-identity). ATTENTION au vocabulaire : "Tenant" ici designe
 * le LOCATAIRE (cahier des charges), un sens different du "tenant"
 * multi-tenant (organisation) utilise partout ailleurs dans le code
 * (TenantClaims, tenant_id). Les deux coexistent car ce sont les termes
 * exacts du cahier des charges et de l'architecture Cornalix d'origine.
 *
 * Les cotitulaires (plusieurs locataires sur un meme bail) ne sont pas
 * geres ici : c'est une relation N:N portee par Lease (voir module lease),
 * exactement comme PropertyOwner porte la relation N:N Property<->Owner.
 */
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 200)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Tenant() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public Tenant(UUID organizationId, String firstName, String lastName, String email, String phone) {
        this.organizationId = organizationId;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
