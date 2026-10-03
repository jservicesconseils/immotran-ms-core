package ca.immotran.core.dashboard;

import ca.immotran.core.application.ApplicationRepository;
import ca.immotran.core.dashboard.dto.DashboardResponse;
import ca.immotran.core.dashboard.dto.RecentApplicationResponse;
import ca.immotran.core.finance.TransactionRepository;
import ca.immotran.core.finance.TransactionType;
import ca.immotran.core.lease.LeaseRepository;
import ca.immotran.core.maintenance.MaintenanceRequestRepository;
import ca.immotran.core.property.PropertyRepository;
import ca.immotran.core.property.UnitRepository;
import ca.immotran.core.property.UnitStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Agrege les donnees de property, finance, maintenance et lease pour une
 * organisation. C'est exactement le benefice du monolithe modulaire :
 * ces requetes traversent plusieurs modules sans aucun appel reseau,
 * juste des jointures JPQL (voir les Repository de chaque module).
 */
@Service
public class DashboardService {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final TransactionRepository transactionRepository;
    private final LeaseRepository leaseRepository;
    private final ApplicationRepository applicationRepository;

    public DashboardService(PropertyRepository propertyRepository, UnitRepository unitRepository,
                             MaintenanceRequestRepository maintenanceRequestRepository,
                             TransactionRepository transactionRepository, LeaseRepository leaseRepository,
                             ApplicationRepository applicationRepository) {
        this.propertyRepository = propertyRepository;
        this.unitRepository = unitRepository;
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.transactionRepository = transactionRepository;
        this.leaseRepository = leaseRepository;
        this.applicationRepository = applicationRepository;
    }

    public DashboardResponse getForOrganization(UUID organizationId) {
        LocalDate today = LocalDate.now();

        List<RecentApplicationResponse> recentApplications = applicationRepository
                .findRecentByOrganizationId(organizationId, PageRequest.of(0, 5)).stream()
                .map(RecentApplicationResponse::from)
                .toList();

        return new DashboardResponse(
                organizationId,
                propertyRepository.countByOrganizationId(organizationId),
                unitRepository.countByOrganizationId(organizationId),
                unitRepository.countByOrganizationIdAndStatus(organizationId, UnitStatus.OCCUPEE),
                unitRepository.countByOrganizationIdAndStatus(organizationId, UnitStatus.DISPONIBLE),
                maintenanceRequestRepository.countOpenByOrganizationId(organizationId),
                transactionRepository.sumAmountByOrganizationIdAndType(organizationId, TransactionType.REVENU),
                transactionRepository.sumAmountByOrganizationIdAndType(organizationId, TransactionType.DEPENSE),
                leaseRepository.countActiveExpiringBetween(organizationId, today, today.plusDays(30)),
                applicationRepository.countOpenByOrganizationId(organizationId),
                recentApplications);
    }
}
