package ca.immotran.core.application;

import ca.immotran.core.application.dto.TenantApplicationStatusResponse;
import ca.immotran.core.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationPublicStatusController.class)
@Import(SecurityConfig.class)
class ApplicationPublicStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void consulterLeStatutPublic_nonAuthentifie_renvoie200() throws Exception {
        UUID applicationId = UUID.randomUUID();
        TenantApplicationStatusResponse response = new TenantApplicationStatusResponse(applicationId, "Jean", "Dupont",
                ApplicationStatus.EN_EVALUATION, "101", "123 rue Principale", "Montreal", "QC",
                new BigDecimal("1250.00"), Instant.now(), null);
        when(applicationService.getPublicStatus(applicationId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/applications/{id}/status", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.status").value("EN_EVALUATION"));
    }

    @Test
    void consulterLeStatutPublic_inexistant_renvoie404() throws Exception {
        UUID applicationId = UUID.randomUUID();
        when(applicationService.getPublicStatus(applicationId)).thenThrow(new ApplicationNotFoundException(applicationId));

        mockMvc.perform(get("/api/v1/applications/{id}/status", applicationId))
                .andExpect(status().isNotFound());
    }
}
