package ca.immotran.core.lease;

import ca.immotran.core.lease.dto.CreateLeaseRequest;
import ca.immotran.core.lease.dto.LeaseResponse;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaseController.class)
@Import(SecurityConfig.class)
class LeaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaseService leaseService;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static PropertyResponse propertyResponse(UUID id, UUID organizationId) {
        return new PropertyResponse(id, organizationId, PropertyType.MAISON_INDIVIDUELLE, null,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1", PropertyStatus.VACANTE,
                null, null, null, null, null, null, null, null, Instant.now());
    }

    private static LeaseResponse leaseResponse(UUID id, UUID propertyId, UUID unitId, List<UUID> tenantIds) {
        return new LeaseResponse(id, propertyId, unitId, tenantIds, LocalDate.of(2026, 1, 1), null,
                new BigDecimal("1500.00"), new BigDecimal("500.00"), null, LeaseStatus.ACTIVE, Instant.now());
    }

    @Test
    void creerUnBail_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(leaseService.create(eq(propertyId), eq(unitId), any()))
                .thenReturn(leaseResponse(leaseId, propertyId, unitId, List.of(tenantId)));

        CreateLeaseRequest request = new CreateLeaseRequest(List.of(tenantId), LocalDate.of(2026, 1, 1), null, new BigDecimal("1500.00"), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId + "/units/" + unitId + "/leases/" + leaseId))
                .andExpect(jsonPath("$.monthlyRent").value(1500.00))
                .andExpect(jsonPath("$.tenantIds[0]").value(tenantId.toString()));
    }

    @Test
    void creerUnBail_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreateLeaseRequest request = new CreateLeaseRequest(List.of(UUID.randomUUID()), LocalDate.of(2026, 1, 1), null, new BigDecimal("1500.00"), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUnBail_sansLocataire_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreateLeaseRequest request = new CreateLeaseRequest(List.of(), LocalDate.of(2026, 1, 1), null, new BigDecimal("1500.00"), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerUnBail_loyerNegatif_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreateLeaseRequest request = new CreateLeaseRequest(List.of(UUID.randomUUID()), LocalDate.of(2026, 1, 1), null, new BigDecimal("-1"), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerUnBail_locataireAutreOrganisation_renvoie409() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(leaseService.create(eq(propertyId), eq(unitId), any()))
                .thenThrow(new LeaseTenantOrganizationMismatchException(tenantId, unitId));

        CreateLeaseRequest request = new CreateLeaseRequest(List.of(tenantId), LocalDate.of(2026, 1, 1), null, new BigDecimal("1500.00"), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void lireUnBail_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(leaseService.getById(unitId, leaseId)).thenReturn(leaseResponse(leaseId, propertyId, unitId, List.of()));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}", propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(leaseId.toString()));
    }

    @Test
    void lireUnBail_inexistant_renvoie404() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(leaseService.getById(unitId, leaseId)).thenThrow(new LeaseNotFoundException(leaseId));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}", propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerLesBaux_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(leaseService.listByUnit(propertyId, unitId)).thenReturn(List.of(leaseResponse(UUID.randomUUID(), propertyId, unitId, List.of())));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/leases", propertyId, unitId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monthlyRent").value(1500.00));
    }

    @Test
    void enregistrerDepotGarantie_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(leaseService.recordSecurityDepositPayment(unitId, leaseId))
                .thenReturn(leaseResponse(leaseId, propertyId, unitId, List.of()));

        mockMvc.perform(put("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/depot-garantie",
                        propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(leaseId.toString()));
    }

    @Test
    void enregistrerDepotGarantie_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        mockMvc.perform(put("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/depot-garantie",
                        propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }
}
