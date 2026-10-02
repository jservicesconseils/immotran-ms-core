package ca.immotran.core.finance;

import java.util.UUID;

/**
 * Levee quand aucune transaction ne correspond a l'id demande.
 * Traduite en reponse HTTP 404 (Not Found) par FinanceExceptionHandler.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(UUID id) {
        super("Aucune transaction trouvee avec l'id " + id);
    }
}
