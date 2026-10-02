immotran-ms-core — service coeur de la gestion immobilière

Rôle dans l'architecture
C'est le service métier principal : il gère les propriétés (maisons, condos, duplex/triplex/quadruplex, immeubles multi-unités), leurs unités, et plus tard les propriétaires, locataires, baux, loyers/dépenses, maintenance, documents, dashboard et règles administratives. C'est un monolithe modulaire volontaire (un seul déploiement, plusieurs packages Java par domaine) — voir la discussion d'architecture : on ne découpe en microservices séparés que lorsqu'une vraie frontière (scaling, équipe, SLA) l'exige.

Ce service ne fait AUCUN appel réseau vers immotran-ms-identity. Il valide lui-même les jetons JWT émis par le même User Pool Cognito, et lit directement le claim tenant_id/tenant_scope (classe TenantClaims, copie volontairement identique à celle d'immotran-ms-identity). Chaque route qui touche une ressource appartenant à une organisation vérifie que le tenant de l'appelant la couvre.

Modèle de données (cahier des charges, §14)
Property 1→N Unit : une maison individuelle est une Property avec UNE SEULE Unit "principale" (principal=true) ; un immeuble est une Property avec N Unit indépendantes. Ce modèle uniforme évite de devoir créer artificiellement plusieurs unités pour une maison, tout en gérant les immeubles multi-unités de la même façon — c'est un critère d'acceptation explicite du MVP (§22).

Modules

Implémenté
- property — Property (type, adresse, province/juridiction, statut) et Unit (étiquette, étage, superficie, chambres/salles de bain, statut, indicateur "principale").

À venir (ordre indicatif, voir §8 et §21 du cahier des charges)
- owner — propriétaires, association N:N avec les propriétés, parts de copropriété
- tenant — locataires, cotitulaires, historique
- lease — baux, versions, renouvellement, résiliation, juridiction
- finance — échéancier de loyers, paiements, dépenses, transactions, rentabilité
- maintenance — tickets, prestataires, historique
- document — upload S3, permissions, versions
- dashboard — occupation, revenus, impayés, baux à échéance
- admin — catalogue de règles provinciales/territoriales, audit

Ce qu'il expose aujourd'hui

POST /api/v1/properties — créer une propriété (tenant vérifié sur organizationId du corps de la requête)
GET /api/v1/properties/{id} — lire une propriété (tenant vérifié sur l'organizationId de la ressource)
POST /api/v1/properties/{propertyId}/units — ajouter une unité à une propriété
GET /api/v1/properties/{propertyId}/units/{unitId} — lire une unité
GET /api/v1/properties/{propertyId}/units — lister les unités d'une propriété

Un point de sécurité important
Contrairement à Organization dans immotran-ms-identity (où l'id de la ressource EST le tenant_id), ici l'id d'une propriété et son organizationId sont deux champs distincts : il faut donc toujours charger la ressource (404 si absente) avant de pouvoir vérifier l'accès au tenant (403 sinon). C'est un compromis assumé — voir les commentaires dans PropertyController.
