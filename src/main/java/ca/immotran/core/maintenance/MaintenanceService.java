package ca.immotran.core.maintenance;

import ca.immotran.core.maintenance.dto.CloseMaintenanceRequestRequest;
import ca.immotran.core.maintenance.dto.CreateMaintenanceRequestRequest;
import ca.immotran.core.maintenance.dto.MaintenanceRequestResponse;
import ca.immotran.core.property.Property;
import ca.immotran.core.property.PropertyService;
import ca.immotran.core.property.Unit;
import ca.immotran.core.property.UnitService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceService {

    private final MaintenanceRequestRepository repository;
    private final PropertyService propertyService;
    private final UnitService unitService;

    public MaintenanceService(MaintenanceRequestRepository repository, PropertyService propertyService,
                               UnitService unitService) {
        this.repository = repository;
        this.propertyService = propertyService;
        this.unitService = unitService;
    }

    public MaintenanceRequestResponse create(UUID propertyId, CreateMaintenanceRequestRequest request) {
        Property property = propertyService.getEntityById(propertyId);
        Unit unit = request.unitId() != null ? unitService.getEntityById(propertyId, request.unitId()) : null;

        MaintenanceRequest saved = repository.save(
                new MaintenanceRequest(property, unit, request.description(), request.priority()));
        return MaintenanceRequestResponse.from(saved);
    }

    public MaintenanceRequestResponse close(UUID propertyId, UUID id, CloseMaintenanceRequestRequest request) {
        MaintenanceRequest maintenanceRequest = getEntityByIdAndProperty(id, propertyId);
        maintenanceRequest.close(request.vendorName(), request.cost());
        return MaintenanceRequestResponse.from(repository.save(maintenanceRequest));
    }

    public MaintenanceRequestResponse getById(UUID propertyId, UUID id) {
        return MaintenanceRequestResponse.from(getEntityByIdAndProperty(id, propertyId));
    }

    public List<MaintenanceRequestResponse> listByProperty(UUID propertyId) {
        return repository.findByPropertyId(propertyId).stream()
                .map(MaintenanceRequestResponse::from)
                .toList();
    }

    private MaintenanceRequest getEntityByIdAndProperty(UUID id, UUID propertyId) {
        MaintenanceRequest request = repository.findById(id)
                .orElseThrow(() -> new MaintenanceRequestNotFoundException(id));

        if (!request.getProperty().getId().equals(propertyId)) {
            throw new MaintenanceRequestNotFoundException(id);
        }

        return request;
    }
}
