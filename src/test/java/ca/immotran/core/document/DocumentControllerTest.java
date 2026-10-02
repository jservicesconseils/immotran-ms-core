package ca.immotran.core.document;

import ca.immotran.core.document.dto.CreateDocumentRequest;
import ca.immotran.core.document.dto.DocumentResponse;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import(SecurityConfig.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static CreateDocumentRequest createRequest(UUID organizationId, UUID entityId) {
        return new CreateDocumentRequest(organizationId, "LEASE", entityId, DocumentType.BAIL, "bail-signe.pdf", "org/lease/bail-signe.pdf");
    }

    private static DocumentResponse response(UUID id, UUID organizationId, UUID entityId) {
        return new DocumentResponse(id, organizationId, "LEASE", entityId, DocumentType.BAIL, "bail-signe.pdf", "org/lease/bail-signe.pdf", Instant.now());
    }

    @Test
    void creerUnDocument_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(documentService.create(any())).thenReturn(response(documentId, organizationId, entityId));

        mockMvc.perform(post("/api/v1/documents")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId, entityId))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/documents/" + documentId))
                .andExpect(jsonPath("$.fileName").value("bail-signe.pdf"));
    }

    @Test
    void creerUnDocument_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/documents")
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(organizationId, entityId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUnDocument_sansJeton_renvoie401() throws Exception {
        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(UUID.randomUUID(), UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lireUnDocument_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(documentService.getById(documentId)).thenReturn(response(documentId, organizationId, entityId));

        mockMvc.perform(get("/api/v1/documents/{id}", documentId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId.toString()));
    }

    @Test
    void lireUnDocument_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(documentService.getById(documentId)).thenReturn(response(documentId, organizationId, entityId));

        mockMvc.perform(get("/api/v1/documents/{id}", documentId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerLesDocuments_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        when(documentService.listForEntity(organizationId, "LEASE", entityId))
                .thenReturn(java.util.List.of(response(UUID.randomUUID(), organizationId, entityId)));

        mockMvc.perform(get("/api/v1/documents")
                        .param("organizationId", organizationId.toString())
                        .param("entityType", "LEASE")
                        .param("entityId", entityId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("bail-signe.pdf"));
    }

    @Test
    void listerLesDocuments_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/documents")
                        .param("organizationId", organizationId.toString())
                        .param("entityType", "LEASE")
                        .param("entityId", entityId.toString())
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString()))))
                .andExpect(status().isForbidden());
    }
}
