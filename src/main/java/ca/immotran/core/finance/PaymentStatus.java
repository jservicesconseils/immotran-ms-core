package ca.immotran.core.finance;

/**
 * Statut d'une echeance de loyer (cahier des charges, §8.9/§11). Le
 * retard ("impaye") n'est volontairement PAS un statut stocke : c'est
 * une donnee derivee (DUE dont la dueDate est passee), calculee a la
 * lecture (voir module dashboard) plutot que maintenue par un job.
 */
public enum PaymentStatus {
    DUE,
    PARTIAL,
    PAID
}
