package ca.immotran.core.application;

import ca.immotran.core.application.dto.ApplicationResponse;
import ca.immotran.core.application.dto.DecideApplicationRequest;
import ca.immotran.core.application.dto.RequestAdditionalInfoRequest;
import ca.immotran.core.application.dto.ReviewApplicationRequest;
import ca.immotran.core.application.dto.SubmitApplicationRequest;
import ca.immotran.core.property.Unit;
import ca.immotran.core.property.UnitService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * CRUD et workflow de decision sur Application. Aucune logique de
 * securite ici : le controleur verifie le tenant_id via TenantClaims
 * pour les actions du personnel -- la soumission (submit), elle, est
 * volontairement publique (voir ApplicationController).
 */
@Service
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationReferenceRepository referenceRepository;
    private final UnitService unitService;

    public ApplicationService(ApplicationRepository repository, ApplicationReferenceRepository referenceRepository,
                               UnitService unitService) {
        this.repository = repository;
        this.referenceRepository = referenceRepository;
        this.unitService = unitService;
    }

    public ApplicationResponse submit(UUID propertyId, UUID unitId, SubmitApplicationRequest request) {
        Unit unit = unitService.getEntityById(propertyId, unitId);

        Application saved = repository.save(new Application(unit, request.firstName(), request.lastName(),
                request.email(), request.phone(), request.employerName(), request.monthlyIncome()));

        List<ApplicationReference> references = request.references().stream()
                .map(reference -> referenceRepository.save(
                        new ApplicationReference(saved, reference.name(), reference.phone(), reference.email())))
                .toList();

        return ApplicationResponse.from(saved, references);
    }

    public ApplicationResponse review(UUID propertyId, UUID unitId, UUID applicationId, ReviewApplicationRequest request) {
        Application application = getEntityByIdAndUnit(applicationId, propertyId, unitId);
        application.review(request.solvencyScore(), request.reviewComments());
        return toResponse(repository.save(application));
    }

    public ApplicationResponse requestAdditionalInfo(UUID propertyId, UUID unitId, UUID applicationId,
                                                      RequestAdditionalInfoRequest request) {
        Application application = getEntityByIdAndUnit(applicationId, propertyId, unitId);
        application.requestAdditionalInfo(request.reviewComments());
        return toResponse(repository.save(application));
    }

    public ApplicationResponse decide(UUID propertyId, UUID unitId, UUID applicationId, DecideApplicationRequest request) {
        Application application = getEntityByIdAndUnit(applicationId, propertyId, unitId);
        application.decide(request.accepted(), request.decisionReason());
        return toResponse(repository.save(application));
    }

    public ApplicationResponse getById(UUID propertyId, UUID unitId, UUID applicationId) {
        return toResponse(getEntityByIdAndUnit(applicationId, propertyId, unitId));
    }

    public List<ApplicationResponse> listByUnit(UUID propertyId, UUID unitId) {
        // Valide au passage que l'unite existe (404 sinon).
        unitService.getEntityById(propertyId, unitId);
        return repository.findByUnitId(unitId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.from(application, referenceRepository.findByApplicationId(application.getId()));
    }

    private Application getEntityByIdAndUnit(UUID applicationId, UUID propertyId, UUID unitId) {
        // Valide au passage que l'unite existe et appartient a la propriete (404 sinon).
        unitService.getEntityById(propertyId, unitId);

        Application application = repository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (!application.getUnit().getId().equals(unitId)) {
            throw new ApplicationNotFoundException(applicationId);
        }

        return application;
    }
}
