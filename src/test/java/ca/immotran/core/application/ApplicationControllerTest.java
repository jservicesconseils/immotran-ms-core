package ca.immotran.core.application;

import ca.immotran.core.application.dto.ApplicationResponse;
import ca.immotran.core.application.dto.DecideApplicationRequest;
import ca.immotran.core.application.dto.ReferenceRequest;
import ca.immotran.core.application.dto.ReferenceResponse;
import ca.immotran.core.application.dto.RequestAdditionalInfoRequest;
import ca.immotran.core.application.dto.ReviewApplicationRequest;
import ca.immotran.core.application.dto.SubmitApplicationRequest;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static PropertyResponse propertyResponse(UUID id, UUID organizationId) {
        return new PropertyResponse(id, organizationId, PropertyType.MAISON_INDIVIDUELLE,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1", PropertyStatus.VACANTE,
                null, null, null, null, null, null, null, null, Instant.now());
    }

    private static SubmitApplicationRequest submitRequest() {
        return new SubmitApplicationRequest("Jean", "Tremblay", "jean.tremblay@example.com", "514-555-0100",
                "Acme inc.", new BigDecimal("4500.00"), List.of(new ReferenceRequest("Marie Leblanc", "514-555-0199", null)));
    }

    private static ApplicationResponse applicationResponse(UUID id, UUID propertyId, UUID unitId, ApplicationStatus status) {
        return new ApplicationResponse(id, propertyId, unitId, "Jean", "Tremblay", "jean.tremblay@example.com",
                "514-555-0100", "Acme inc.", new BigDecimal("4500.00"), status, null, null, null,
                List.of(new ReferenceResponse(UUID.randomUUID(), "Marie Leblanc", "514-555-0199", null)),
                Instant.now(), null);
    }

    @Test
    void soumettreUneCandidature_nonAuthentifie_renvoie201() throws Exception {
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(applicationService.submit(eq(propertyId), eq(unitId), any()))
                .thenReturn(applicationResponse(applicationId, propertyId, unitId, ApplicationStatus.EN_ATTENTE_VERIFICATION));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/applications", propertyId, unitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EN_ATTENTE_VERIFICATION"))
                .andExpect(jsonPath("$.references[0].name").value("Marie Leblanc"));
    }

    @Test
    void soumettreUneCandidature_sansReference_renvoie400() throws Exception {
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        SubmitApplicationRequest invalide = new SubmitApplicationRequest("Jean", "Tremblay",
                "jean.tremblay@example.com", "514-555-0100", "Acme inc.", new BigDecimal("4500.00"), List.of());

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/applications", propertyId, unitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lireUneCandidature_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(applicationService.getById(propertyId, unitId, applicationId))
                .thenReturn(applicationResponse(applicationId, propertyId, unitId, ApplicationStatus.EN_ATTENTE_VERIFICATION));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/applications/{applicationId}", propertyId, unitId, applicationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jean.tremblay@example.com"));
    }

    @Test
    void lireUneCandidature_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/applications/{applicationId}", propertyId, unitId, applicationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerLesCandidatures_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(applicationService.listByUnit(propertyId, unitId)).thenReturn(
                List.of(applicationResponse(UUID.randomUUID(), propertyId, unitId, ApplicationStatus.EN_ATTENTE_VERIFICATION)));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/applications", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Tremblay"));
    }

    @Test
    void evaluerUneCandidature_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(applicationService.review(eq(propertyId), eq(unitId), eq(applicationId), any()))
                .thenReturn(applicationResponse(applicationId, propertyId, unitId, ApplicationStatus.EN_EVALUATION));

        ReviewApplicationRequest request = new ReviewApplicationRequest(78, "Dossier solide");

        mockMvc.perform(put("/api/v1/properties/{propertyId}/units/{unitId}/applications/{applicationId}/evaluation",
                        propertyId, unitId, applicationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_EVALUATION"));
    }

    @Test
    void demanderInformationComplementaire_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(applicationService.requestAdditionalInfo(eq(propertyId), eq(unitId), eq(applicationId), any()))
                .thenReturn(applicationResponse(applicationId, propertyId, unitId, ApplicationStatus.EN_ATTENTE_INFO));

        RequestAdditionalInfoRequest request = new RequestAdditionalInfoRequest("Preuve de revenu manquante");

        mockMvc.perform(put("/api/v1/properties/{propertyId}/units/{unitId}/applications/{applicationId}/demande-information",
                        propertyId, unitId, applicationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_ATTENTE_INFO"));
    }

    @Test
    void deciderUneCandidature_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(applicationService.decide(eq(propertyId), eq(unitId), eq(applicationId), any()))
                .thenReturn(applicationResponse(applicationId, propertyId, unitId, ApplicationStatus.ACCEPTEE));

        DecideApplicationRequest request = new DecideApplicationRequest(true, "Revenu suffisant, references verifiees");

        mockMvc.perform(put("/api/v1/properties/{propertyId}/units/{unitId}/applications/{applicationId}/decision",
                        propertyId, unitId, applicationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTEE"));
    }

    @Test
    void deciderUneCandidature_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        DecideApplicationRequest request = new DecideApplicationRequest(false, "Revenu insuffisant");

        mockMvc.perform(put("/api/v1/properties/{propertyId}/units/{unitId}/applications/{applicationId}/decision",
                        propertyId, unitId, applicationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
