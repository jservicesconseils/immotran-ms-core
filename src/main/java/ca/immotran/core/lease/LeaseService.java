package ca.immotran.core.lease;

import ca.immotran.core.lease.dto.CreateLeaseRequest;
import ca.immotran.core.lease.dto.LeaseResponse;
import ca.immotran.core.property.Unit;
import ca.immotran.core.property.UnitService;
import ca.immotran.core.tenant.Tenant;
import ca.immotran.core.tenant.TenantService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * CRUD sur Lease et sa relation N:N avec Tenant (cotitulaires). Aucune
 * logique de securite ici : le controleur verifie le tenant_id (SaaS)
 * via TenantClaims avant/apres appel.
 */
@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final LeaseTenantRepository leaseTenantRepository;
    private final UnitService unitService;
    private final TenantService tenantService;

    public LeaseService(LeaseRepository leaseRepository, LeaseTenantRepository leaseTenantRepository,
                         UnitService unitService, TenantService tenantService) {
        this.leaseRepository = leaseRepository;
        this.leaseTenantRepository = leaseTenantRepository;
        this.unitService = unitService;
        this.tenantService = tenantService;
    }

    public LeaseResponse create(UUID propertyId, UUID unitId, CreateLeaseRequest request) {
        Unit unit = unitService.getEntityById(propertyId, unitId);
        UUID organizationId = unit.getProperty().getOrganizationId();

        List<Tenant> tenants = request.tenantIds().stream()
                .map(tenantService::getEntityById)
                .toList();

        for (Tenant tenant : tenants) {
            if (!tenant.getOrganizationId().equals(organizationId)) {
                throw new LeaseTenantOrganizationMismatchException(tenant.getId(), unitId);
            }
        }

        Lease savedLease = leaseRepository.save(
                new Lease(unit, request.startDate(), request.endDate(), request.monthlyRent(), request.securityDeposit()));

        List<LeaseTenant> leaseTenants = tenants.stream()
                .map(tenant -> leaseTenantRepository.save(new LeaseTenant(savedLease, tenant)))
                .toList();

        // La signature du bail vaut prise de possession (cahier des
        // charges, §4) : l'unite n'est plus disponible pour un autre bail.
        unitService.markOccupied(unit);

        return LeaseResponse.from(savedLease, leaseTenants);
    }

    public LeaseResponse recordSecurityDepositPayment(UUID unitId, UUID leaseId) {
        Lease lease = getEntityByIdAndUnit(leaseId, unitId);
        lease.recordSecurityDepositPayment(Instant.now());
        Lease saved = leaseRepository.save(lease);
        return LeaseResponse.from(saved, leaseTenantRepository.findByLeaseId(leaseId));
    }

    public LeaseResponse getById(UUID unitId, UUID leaseId) {
        Lease lease = getEntityByIdAndUnit(leaseId, unitId);
        return LeaseResponse.from(lease, leaseTenantRepository.findByLeaseId(leaseId));
    }

    // Public (comme PropertyService/UnitService.getEntityById) : le
    // module finance a besoin de l'entite Lease brute pour sa propre
    // relation JPA (Payment).
    public Lease getEntityById(UUID unitId, UUID leaseId) {
        return getEntityByIdAndUnit(leaseId, unitId);
    }

    public List<LeaseResponse> listByUnit(UUID propertyId, UUID unitId) {
        // Valide au passage que l'unite existe (404 sinon).
        unitService.getEntityById(propertyId, unitId);
        return leaseRepository.findByUnitId(unitId).stream()
                .map(lease -> LeaseResponse.from(lease, leaseTenantRepository.findByLeaseId(lease.getId())))
                .toList();
    }

    private Lease getEntityByIdAndUnit(UUID leaseId, UUID unitId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new LeaseNotFoundException(leaseId));

        if (!lease.getUnit().getId().equals(unitId)) {
            throw new LeaseNotFoundException(leaseId);
        }

        return lease;
    }
}
