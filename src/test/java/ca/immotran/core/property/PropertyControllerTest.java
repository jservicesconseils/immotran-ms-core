package ca.immotran.core.property;

import ca.immotran.core.property.dto.CreatePropertyRequest;
import ca.immotran.core.property.dto.CreateUnitRequest;
import ca.immotran.core.property.dto.PropertyResponse;
import ca.immotran.core.property.dto.UnitResponse;
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
import java.util.List;
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

/**
 * Test unitaire de la tranche web (@WebMvcTest) : on monte le controleur
 * + la vraie configuration de securite (@Import(SecurityConfig.class))
 * sans base de donnees ni serveur reel, ni vrai Cognito.
 *
 * - PropertyService/UnitService sont mockes (@MockBean) : on isole le
 *   controleur, et on ne manipule jamais d'entite JPA ici, seulement des
 *   DTO (PropertyResponse/UnitResponse), exactement ce que renvoient les
 *   vrais services.
 * - JwtDecoder est mocke (@MockBean) : necessaire pour que le filtre de
 *   securite se construise, mais jamais reellement appele -- le
 *   post-processor jwt() injecte directement un utilisateur "authentifie"
 *   dans le contexte de securite du test, sans decoder un vrai jeton.
 */
@WebMvcTest(PropertyController.class)
@Import(SecurityConfig.class)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private UnitService unitService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static CreatePropertyRequest createRequest(UUID organizationId) {
        return new CreatePropertyRequest(organizationId, PropertyType.MAISON_INDIVIDUELLE,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1");
    }

    private static PropertyResponse response(UUID id, UUID organizationId) {
        return new PropertyResponse(id, organizationId, PropertyType.MAISON_INDIVIDUELLE,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1", PropertyStatus.VACANTE, Instant.now());
    }

    @Test
    void creerUnePropriete_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.create(any())).thenReturn(response(propertyId, organizationId));

        mockMvc.perform(post("/api/v1/properties")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId))
                .andExpect(jsonPath("$.id").value(propertyId.toString()))
                .andExpect(jsonPath("$.organizationId").value(organizationId.toString()));
    }

    @Test
    void creerUnePropriete_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/properties")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUnePropriete_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerUnePropriete_typeManquant_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        String jsonInvalide = "{\"organizationId\":\"" + organizationId + "\",\"street\":\"123 rue\",\"city\":\"Montreal\",\"province\":\"QC\",\"postalCode\":\"H1A 1A1\"}";

        mockMvc.perform(post("/api/v1/properties")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalide))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lireUnePropriete_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(response(propertyId, organizationId));

        mockMvc.perform(get("/api/v1/properties/{id}", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propertyId.toString()));
    }

    @Test
    void lireUnePropriete_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(response(propertyId, organizationId));

        mockMvc.perform(get("/api/v1/properties/{id}", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void lireUnePropriete_inexistante_renvoie404() throws Exception {
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenThrow(new PropertyNotFoundException(propertyId));

        mockMvc.perform(get("/api/v1/properties/{id}", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void lireUnePropriete_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listerLesProprietes_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        when(propertyService.listByOrganization(organizationId)).thenReturn(List.of(response(UUID.randomUUID(), organizationId)));

        mockMvc.perform(get("/api/v1/properties")
                        .param("organizationId", organizationId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizationId").value(organizationId.toString()));
    }

    @Test
    void listerLesProprietes_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/properties")
                        .param("organizationId", organizationId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void ajouterUneUnite_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(response(propertyId, organizationId));
        when(unitService.create(eq(propertyId), any())).thenReturn(
                new UnitResponse(unitId, propertyId, "Principal", true, null, null, 3, 1, UnitStatus.DISPONIBLE, Instant.now()));

        CreateUnitRequest request = new CreateUnitRequest("Principal", true, null, null, 3, 1);

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId + "/units/" + unitId))
                .andExpect(jsonPath("$.label").value("Principal"))
                .andExpect(jsonPath("$.principal").value(true));
    }

    @Test
    void ajouterUneUnite_proprieteDansAutreTenant_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(response(propertyId, organizationId));

        CreateUnitRequest request = new CreateUnitRequest("304", false, 3, 55.5, 2, 1);

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerLesUnites_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(response(propertyId, organizationId));
        when(unitService.listByProperty(propertyId)).thenReturn(List.of(
                new UnitResponse(UUID.randomUUID(), propertyId, "Principal", true, null, null, 3, 1, UnitStatus.DISPONIBLE, Instant.now())));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Principal"));
    }
}
