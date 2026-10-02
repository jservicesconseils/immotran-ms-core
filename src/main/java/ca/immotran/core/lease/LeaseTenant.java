package ca.immotran.core.lease;

import ca.immotran.core.tenant.Tenant;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * Association N:N entre Lease et Tenant (cotitulaires, cahier des
 * charges §10 "Un ou plusieurs locataires"), meme principe que
 * PropertyOwner pour Property<->Owner.
 */
@Entity
@Table(name = "lease_tenants", uniqueConstraints = @UniqueConstraint(columnNames = {"lease_id", "tenant_id"}))
public class LeaseTenant {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "lease_id", nullable = false)
    private Lease lease;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    private Instant createdAt;

    protected LeaseTenant() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public LeaseTenant(Lease lease, Tenant tenant) {
        this.lease = lease;
        this.tenant = tenant;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Lease getLease() {
        return lease;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
