package ca.immotran.core.admin;

import ca.immotran.core.admin.dto.CreateJurisdictionRuleRequest;
import ca.immotran.core.admin.dto.JurisdictionRuleResponse;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pas de verification de tenant ici (donnee de reference partagee) :
 * seul un jeton valide est requis, voir JurisdictionRuleController.
 */
@WebMvcTest(JurisdictionRuleController.class)
@Import(SecurityConfig.class)
class JurisdictionRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JurisdictionRuleService service;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static CreateJurisdictionRuleRequest createRequest() {
        return new CreateJurisdictionRuleRequest("QC", "PREAVIS_RESILIATION",
                "Preavis minimal de 3 mois pour un bail a duree indeterminee", LocalDate.of(2026, 1, 1),
                "https://www.quebec.ca/habitation/louer-logement", 1);
    }

    private static JurisdictionRuleResponse response(UUID id) {
        return new JurisdictionRuleResponse(id, "QC", "PREAVIS_RESILIATION",
                "Preavis minimal de 3 mois pour un bail a duree indeterminee", LocalDate.of(2026, 1, 1),
                "https://www.quebec.ca/habitation/louer-logement", 1, Instant.now());
    }

    @Test
    void creerUneRegle_avecJetonValide_renvoie201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(any())).thenReturn(response(id));

        mockMvc.perform(post("/api/v1/admin/jurisdiction-rules")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/admin/jurisdiction-rules/" + id))
                .andExpect(jsonPath("$.province").value("QC"));
    }

    @Test
    void creerUneRegle_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/jurisdiction-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerUneRegle_provinceInvalide_renvoie400() throws Exception {
        CreateJurisdictionRuleRequest invalide = new CreateJurisdictionRuleRequest("QUEBEC", "PREAVIS_RESILIATION",
                "Description", LocalDate.of(2026, 1, 1), "https://example.com", 1);

        mockMvc.perform(post("/api/v1/admin/jurisdiction-rules")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lireUneRegle_inexistante_renvoie404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new JurisdictionRuleNotFoundException(id));

        mockMvc.perform(get("/api/v1/admin/jurisdiction-rules/{id}", id).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerLesRegles_parProvince_renvoie200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.listByProvince("QC")).thenReturn(java.util.List.of(response(id)));

        mockMvc.perform(get("/api/v1/admin/jurisdiction-rules").param("province", "QC").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].province").value("QC"));
    }
}
