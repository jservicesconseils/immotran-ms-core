package ca.immotran.core.finance;

import java.util.UUID;

/**
 * Levee quand aucun paiement ne correspond a l'id demande pour le bail
 * donne. Traduite en reponse HTTP 404 (Not Found) par FinanceExceptionHandler.
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(UUID id) {
        super("Aucun paiement trouve avec l'id " + id);
    }
}
