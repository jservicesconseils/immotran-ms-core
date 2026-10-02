package ca.immotran.core.admin;

import ca.immotran.core.admin.dto.AuditLogResponse;
import ca.immotran.core.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@Import(SecurityConfig.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void listerLesEntrees_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        when(auditLogService.listForOrganization(organizationId)).thenReturn(java.util.List.of(
                new AuditLogResponse(UUID.randomUUID(), organizationId, "user-1", "CREATE_PROPERTY", "PROPERTY", resourceId, Instant.now())));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/audit-logs", organizationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE_PROPERTY"));
    }

    @Test
    void listerLesEntrees_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/audit-logs", organizationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerLesEntrees_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/audit-logs", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
