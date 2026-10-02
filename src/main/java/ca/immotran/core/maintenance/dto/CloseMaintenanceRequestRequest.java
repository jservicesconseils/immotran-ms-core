package ca.immotran.core.maintenance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Ce que le client envoie pour cloturer une demande de maintenance. */
public record CloseMaintenanceRequestRequest(

        @Size(max = 200, message = "le nom du prestataire ne doit pas depasser 200 caracteres")
        String vendorName,

        @DecimalMin(value = "0.0", message = "le cout ne peut pas etre negatif")
        BigDecimal cost
) {
}
