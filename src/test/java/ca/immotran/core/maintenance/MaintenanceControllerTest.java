package ca.immotran.core.maintenance;

import ca.immotran.core.maintenance.dto.CloseMaintenanceRequestRequest;
import ca.immotran.core.maintenance.dto.CreateMaintenanceRequestRequest;
import ca.immotran.core.maintenance.dto.MaintenanceRequestResponse;
import ca.immotran.core.property.PropertyService;
import ca.immotran.core.property.PropertyStatus;
import ca.immotran.core.property.PropertyType;
import ca.immotran.core.property.dto.PropertyResponse;
import ca.immotran.core.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceController.class)
@Import(SecurityConfig.class)
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceService maintenanceService;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static PropertyResponse propertyResponse(UUID id, UUID organizationId) {
        return new PropertyResponse(id, organizationId, PropertyType.MAISON_INDIVIDUELLE,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1", PropertyStatus.VACANTE,
                null, null, null, null, null, null, null, null, Instant.now());
    }

    private static MaintenanceRequestResponse response(UUID id, UUID propertyId, MaintenanceStatus status) {
        return new MaintenanceRequestResponse(id, propertyId, null, "Fuite robinet cuisine",
                MaintenancePriority.NORMALE, status, null, null, Instant.now(), null);
    }

    @Test
    void creerUneDemande_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(maintenanceService.create(eq(propertyId), any())).thenReturn(response(requestId, propertyId, MaintenanceStatus.OUVERTE));

        CreateMaintenanceRequestRequest request = new CreateMaintenanceRequestRequest(null, "Fuite robinet cuisine", MaintenancePriority.NORMALE);

        mockMvc.perform(post("/api/v1/properties/{propertyId}/maintenance-requests", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId + "/maintenance-requests/" + requestId))
                .andExpect(jsonPath("$.status").value("OUVERTE"));
    }

    @Test
    void creerUneDemande_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreateMaintenanceRequestRequest request = new CreateMaintenanceRequestRequest(null, "Fuite robinet cuisine", MaintenancePriority.NORMALE);

        mockMvc.perform(post("/api/v1/properties/{propertyId}/maintenance-requests", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUneDemande_descriptionManquante_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreateMaintenanceRequestRequest request = new CreateMaintenanceRequestRequest(null, " ", MaintenancePriority.NORMALE);

        mockMvc.perform(post("/api/v1/properties/{propertyId}/maintenance-requests", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cloturerUneDemande_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(maintenanceService.close(eq(propertyId), eq(requestId), any()))
                .thenReturn(response(requestId, propertyId, MaintenanceStatus.TERMINEE));

        CloseMaintenanceRequestRequest request = new CloseMaintenanceRequestRequest("Plomberie ABC", new BigDecimal("150.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/maintenance-requests/{id}/close", propertyId, requestId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINEE"));
    }

    @Test
    void lireUneDemande_inexistante_renvoie404() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(maintenanceService.getById(propertyId, requestId)).thenThrow(new MaintenanceRequestNotFoundException(requestId));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/maintenance-requests/{id}", propertyId, requestId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerLesDemandes_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(maintenanceService.listByProperty(propertyId)).thenReturn(
                java.util.List.of(response(UUID.randomUUID(), propertyId, MaintenanceStatus.OUVERTE)));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/maintenance-requests", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OUVERTE"));
    }
}
