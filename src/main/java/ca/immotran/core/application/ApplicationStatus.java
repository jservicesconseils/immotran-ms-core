package ca.immotran.core.application;

/**
 * Statut d'une candidature de location, suivant le processus reel de
 * location (soumission -> verification -> evaluation -> decision), avec
 * un etat de pause EN_ATTENTE_INFO quand un document manque.
 */
public enum ApplicationStatus {
    EN_ATTENTE_VERIFICATION,
    EN_EVALUATION,
    EN_ATTENTE_INFO,
    ACCEPTEE,
    REFUSEE
}
