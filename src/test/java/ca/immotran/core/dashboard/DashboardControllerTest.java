package ca.immotran.core.dashboard;

import ca.immotran.core.dashboard.dto.DashboardResponse;
import ca.immotran.core.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void lireLeDashboard_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        when(dashboardService.getForOrganization(organizationId)).thenReturn(new DashboardResponse(
                organizationId, 5, 12, 9, 3, 2, new BigDecimal("18000.00"), new BigDecimal("3200.00"), 1));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/dashboard", organizationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProperties").value(5))
                .andExpect(jsonPath("$.occupiedUnits").value(9))
                .andExpect(jsonPath("$.totalRevenue").value(18000.00));
    }

    @Test
    void lireLeDashboard_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/dashboard", organizationId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void lireLeDashboard_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/dashboard", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
