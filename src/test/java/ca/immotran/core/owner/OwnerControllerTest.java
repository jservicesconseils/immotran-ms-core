package ca.immotran.core.owner;

import ca.immotran.core.owner.dto.AttachOwnerRequest;
import ca.immotran.core.owner.dto.CreateOwnerRequest;
import ca.immotran.core.owner.dto.OwnerResponse;
import ca.immotran.core.owner.dto.PropertyOwnerResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Meme approche que PropertyControllerTest : @WebMvcTest + vraie
 * SecurityConfig, services mockes (DTO uniquement, jamais d'entite JPA),
 * JwtDecoder mocke pour que le filtre de securite se construise.
 */
@WebMvcTest(OwnerController.class)
@Import(SecurityConfig.class)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OwnerService ownerService;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static CreateOwnerRequest createRequest(UUID organizationId) {
        return new CreateOwnerRequest(organizationId, OwnerType.PARTICULIER, "Jean Tremblay", "jean@example.com", "514-555-0100");
    }

    private static OwnerResponse ownerResponse(UUID id, UUID organizationId) {
        return new OwnerResponse(id, organizationId, OwnerType.PARTICULIER, "Jean Tremblay", "jean@example.com", "514-555-0100", Instant.now());
    }

    private static PropertyResponse propertyResponse(UUID id, UUID organizationId) {
        return new PropertyResponse(id, organizationId, PropertyType.MAISON_INDIVIDUELLE, null,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1", PropertyStatus.VACANTE,
                null, null, null, null, null, null, null, null, Instant.now());
    }

    @Test
    void creerUnProprietaire_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(ownerService.create(any())).thenReturn(ownerResponse(ownerId, organizationId));

        mockMvc.perform(post("/api/v1/owners")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/owners/" + ownerId))
                .andExpect(jsonPath("$.name").value("Jean Tremblay"));
    }

    @Test
    void creerUnProprietaire_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/owners")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUnProprietaire_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(post("/api/v1/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creerUnProprietaire_courrielInvalide_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        CreateOwnerRequest invalide = new CreateOwnerRequest(organizationId, OwnerType.PARTICULIER, "Jean Tremblay", "pas-un-courriel", null);

        mockMvc.perform(post("/api/v1/owners")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lireUnProprietaire_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(ownerService.getById(ownerId)).thenReturn(ownerResponse(ownerId, organizationId));

        mockMvc.perform(get("/api/v1/owners/{id}", ownerId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownerId.toString()));
    }

    @Test
    void lireUnProprietaire_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(ownerService.getById(ownerId)).thenReturn(ownerResponse(ownerId, organizationId));

        mockMvc.perform(get("/api/v1/owners/{id}", ownerId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void lireUnProprietaire_inexistant_renvoie404() throws Exception {
        UUID ownerId = UUID.randomUUID();
        when(ownerService.getById(ownerId)).thenThrow(new OwnerNotFoundException(ownerId));

        mockMvc.perform(get("/api/v1/owners/{id}", ownerId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerLesProprietairesDUneOrganisation_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        when(ownerService.listByOrganization(organizationId)).thenReturn(List.of(ownerResponse(UUID.randomUUID(), organizationId)));

        mockMvc.perform(get("/api/v1/owners")
                        .param("organizationId", organizationId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jean Tremblay"));
    }

    @Test
    void listerLesProprietairesDUneOrganisation_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/owners")
                        .param("organizationId", organizationId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void associerUnProprietaire_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID associationId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(ownerService.attachToProperty(org.mockito.ArgumentMatchers.eq(propertyId), any())).thenReturn(
                new PropertyOwnerResponse(associationId, propertyId, ownerId, "Jean Tremblay", new BigDecimal("100.00"), Instant.now()));

        AttachOwnerRequest request = new AttachOwnerRequest(ownerId, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/owners", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId + "/owners/" + associationId))
                .andExpect(jsonPath("$.ownerName").value("Jean Tremblay"));
    }

    @Test
    void associerUnProprietaire_proprieteDansAutreTenant_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        AttachOwnerRequest request = new AttachOwnerRequest(UUID.randomUUID(), new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/owners", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void associerUnProprietaire_partSuperieureA100_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        AttachOwnerRequest request = new AttachOwnerRequest(UUID.randomUUID(), new BigDecimal("150.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/owners", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void associerUnProprietaire_dejaAssocie_renvoie409() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(ownerService.attachToProperty(org.mockito.ArgumentMatchers.eq(propertyId), any()))
                .thenThrow(new OwnerAlreadyAttachedException(ownerId, propertyId));

        AttachOwnerRequest request = new AttachOwnerRequest(ownerId, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/owners", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void associerUnProprietaire_organisationDifferente_renvoie409() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(ownerService.attachToProperty(org.mockito.ArgumentMatchers.eq(propertyId), any()))
                .thenThrow(new OwnerOrganizationMismatchException(ownerId, propertyId));

        AttachOwnerRequest request = new AttachOwnerRequest(ownerId, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/owners", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void listerLesProprietaires_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(ownerService.listForProperty(propertyId)).thenReturn(java.util.List.of(
                new PropertyOwnerResponse(UUID.randomUUID(), propertyId, UUID.randomUUID(), "Jean Tremblay", new BigDecimal("100.00"), Instant.now())));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/owners", propertyId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerName").value("Jean Tremblay"));
    }
}
