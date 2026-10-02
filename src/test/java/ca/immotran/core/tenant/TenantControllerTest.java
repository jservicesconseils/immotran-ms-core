package ca.immotran.core.tenant;

import ca.immotran.core.security.SecurityConfig;
import ca.immotran.core.tenant.dto.CreateTenantRequest;
import ca.immotran.core.tenant.dto.TenantResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TenantController.class)
@Import(SecurityConfig.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static CreateTenantRequest createRequest(UUID organizationId) {
        return new CreateTenantRequest(organizationId, "Marie", "Gagnon", "marie@example.com", "514-555-0101");
    }

    private static TenantResponse response(UUID id, UUID organizationId) {
        return new TenantResponse(id, organizationId, "Marie", "Gagnon", "marie@example.com", "514-555-0101", Instant.now());
    }

    @Test
    void creerUnLocataire_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(tenantService.create(any())).thenReturn(response(tenantId, organizationId));

        mockMvc.perform(post("/api/v1/tenants")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tenants/" + tenantId))
                .andExpect(jsonPath("$.firstName").value("Marie"));
    }

    @Test
    void creerUnLocataire_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/tenants")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUnLocataire_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lireUnLocataire_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(tenantService.getById(tenantId)).thenReturn(response(tenantId, organizationId));

        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenantId.toString()));
    }

    @Test
    void lireUnLocataire_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(tenantService.getById(tenantId)).thenReturn(response(tenantId, organizationId));

        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerLesLocataires_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        when(tenantService.listByOrganization(organizationId)).thenReturn(List.of(response(UUID.randomUUID(), organizationId)));

        mockMvc.perform(get("/api/v1/tenants")
                        .param("organizationId", organizationId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Marie"));
    }

    @Test
    void listerLesLocataires_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/tenants")
                        .param("organizationId", organizationId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void lireUnLocataire_inexistant_renvoie404() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(tenantService.getById(tenantId)).thenThrow(new TenantNotFoundException(tenantId));

        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }
}
