package ca.immotran.core.finance;

import ca.immotran.core.finance.dto.CreatePaymentRequest;
import ca.immotran.core.finance.dto.PaymentResponse;
import ca.immotran.core.finance.dto.RecordPaymentRequest;
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

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static PropertyResponse propertyResponse(UUID id, UUID organizationId) {
        return new PropertyResponse(id, organizationId, PropertyType.MAISON_INDIVIDUELLE,
                "123 rue des Lilas", "Montreal", "QC", "H1A 1A1", PropertyStatus.VACANTE,
                null, null, null, null, null, null, null, null, Instant.now());
    }

    private static PaymentResponse paymentResponse(UUID id, UUID leaseId, BigDecimal amountDue, BigDecimal amountPaid, PaymentStatus status) {
        return new PaymentResponse(id, leaseId, LocalDate.of(2026, 2, 1), amountDue, amountPaid, null, status, Instant.now());
    }

    @Test
    void creerUneEcheance_tenantCorrespondant_renvoie201() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(paymentService.create(eq(unitId), eq(leaseId), any()))
                .thenReturn(paymentResponse(paymentId, leaseId, new BigDecimal("1500.00"), BigDecimal.ZERO, PaymentStatus.DUE));

        CreatePaymentRequest request = new CreatePaymentRequest(LocalDate.of(2026, 2, 1), new BigDecimal("1500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments", propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/properties/" + propertyId + "/units/" + unitId
                        + "/leases/" + leaseId + "/payments/" + paymentId))
                .andExpect(jsonPath("$.status").value("DUE"));
    }

    @Test
    void creerUneEcheance_tenantDifferent_renvoie403() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID autreTenant = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreatePaymentRequest request = new CreatePaymentRequest(LocalDate.of(2026, 2, 1), new BigDecimal("1500.00"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments", propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", autreTenant.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void creerUneEcheance_montantNegatif_renvoie400() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));

        CreatePaymentRequest request = new CreatePaymentRequest(LocalDate.of(2026, 2, 1), new BigDecimal("-10"));

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments", propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enregistrerUnPaiement_partiel_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(paymentService.recordPayment(eq(leaseId), eq(paymentId), any()))
                .thenReturn(paymentResponse(paymentId, leaseId, new BigDecimal("1500.00"), new BigDecimal("500.00"), PaymentStatus.PARTIAL));

        RecordPaymentRequest request = new RecordPaymentRequest(new BigDecimal("500.00"), null);

        mockMvc.perform(post("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments/{paymentId}/record",
                        propertyId, unitId, leaseId, paymentId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.amountPaid").value(500.00));
    }

    @Test
    void lireUneEcheance_inexistante_renvoie404() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(paymentService.getById(leaseId, paymentId)).thenThrow(new PaymentNotFoundException(paymentId));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments/{paymentId}",
                        propertyId, unitId, leaseId, paymentId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listerLesEcheances_tenantCorrespondant_renvoie200() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        when(propertyService.getById(propertyId)).thenReturn(propertyResponse(propertyId, organizationId));
        when(paymentService.listByLease(leaseId)).thenReturn(java.util.List.of(
                paymentResponse(UUID.randomUUID(), leaseId, new BigDecimal("1500.00"), BigDecimal.ZERO, PaymentStatus.DUE)));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments", propertyId, unitId, leaseId)
                        .with(jwt().jwt(builder -> builder.claim("tenant_id", organizationId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DUE"));
    }
}
