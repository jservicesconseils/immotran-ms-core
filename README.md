immotran-ms-core — service coeur de la gestion immobilière

Rôle dans l'architecture
C'est le service métier principal : il gère les propriétés (maisons, condos, duplex/triplex/quadruplex, immeubles multi-unités), leurs unités, et plus tard les propriétaires, locataires, baux, loyers/dépenses, maintenance, documents, dashboard et règles administratives. C'est un monolithe modulaire volontaire (un seul déploiement, plusieurs packages Java par domaine) — voir la discussion d'architecture : on ne découpe en microservices séparés que lorsqu'une vraie frontière (scaling, équipe, SLA) l'exige.

Ce service ne fait AUCUN appel réseau vers immotran-ms-identity. Il valide lui-même les jetons JWT émis par le même User Pool Cognito, et lit directement le claim tenant_id/tenant_scope (classe TenantClaims, copie volontairement identique à celle d'immotran-ms-identity). Chaque route qui touche une ressource appartenant à une organisation vérifie que le tenant de l'appelant la couvre.

Modèle de données (cahier des charges, §14)
Property 1→N Unit : une maison individuelle est une Property avec UNE SEULE Unit "principale" (principal=true) ; un immeuble est une Property avec N Unit indépendantes. Ce modèle uniforme évite de devoir créer artificiellement plusieurs unités pour une maison, tout en gérant les immeubles multi-unités de la même façon — c'est un critère d'acceptation explicite du MVP (§22).

Modules

Implémenté
- property — Property (type, adresse, province/juridiction, statut) et Unit (étiquette, étage, superficie, chambres/salles de bain, statut, indicateur "principale").
- owner — Owner (particulier ou société) et son association N:N avec Property via PropertyOwner (part de propriété en %, contrainte d'unicité par couple propriété/propriétaire, validation que les deux appartiennent à la même organisation).
- tenant — Tenant (locataire). ATTENTION au vocabulaire : désigne le locataire (cahier des charges), pas le tenant SaaS (organisation) utilisé partout ailleurs (TenantClaims) — voir le commentaire en tête de Tenant.java.
- lease — Lease (bail) rattaché à une Unit, avec cotitulaires via LeaseTenant (N:N avec Tenant, même principe que PropertyOwner). Validation que chaque locataire appartient à la même organisation que l'unité.
- finance — Payment (échéancier de loyer rattaché à un bail, paiement complet/partiel via recordPayment) et Transaction (revenus/dépenses au niveau propriété, par catégorie).
- maintenance — MaintenanceRequest (propriété, unité optionnelle ou espace commun), cycle de vie OUVERTE → EN_COURS/TERMINEE avec prestataire et coût à la clôture.
- document — Document : métadonnées seulement (référence polymorphe entityType/entityId vers n'importe quel module). L'upload réel (S3, URL présignée) est hors scope de ce recit — voir Document.java.

À venir (ordre indicatif, voir §8 et §21 du cahier des charges)
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
POST /api/v1/owners — créer un propriétaire
GET /api/v1/owners/{id} — lire un propriétaire
POST /api/v1/properties/{propertyId}/owners — associer un propriétaire existant à une propriété, avec sa part
GET /api/v1/properties/{propertyId}/owners — lister les propriétaires d'une propriété
POST /api/v1/tenants — créer un locataire
GET /api/v1/tenants/{id} — lire un locataire
POST /api/v1/properties/{propertyId}/units/{unitId}/leases — créer un bail (un ou plusieurs locataires)
GET /api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId} — lire un bail
GET /api/v1/properties/{propertyId}/units/{unitId}/leases — lister les baux d'une unité (historique)
POST /api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments — créer une échéance de loyer
POST /api/v1/properties/{propertyId}/units/{unitId}/leases/{leaseId}/payments/{paymentId}/record — enregistrer un paiement (complet ou partiel)
GET .../payments/{paymentId} et GET .../payments — lire / lister les échéances d'un bail
POST /api/v1/properties/{propertyId}/transactions — enregistrer un revenu ou une dépense
GET /api/v1/properties/{propertyId}/transactions/{id} et GET .../transactions — lire / lister les transactions d'une propriété
POST /api/v1/properties/{propertyId}/maintenance-requests — créer une demande de maintenance
POST .../maintenance-requests/{id}/close — clôturer avec prestataire et coût
GET .../maintenance-requests/{id} et GET .../maintenance-requests — lire / lister les demandes d'une propriété
POST /api/v1/documents — enregistrer les métadonnées d'un document
GET /api/v1/documents/{id} — lire un document
GET /api/v1/documents?organizationId=&entityType=&entityId= — lister les documents d'une entité

Un point de sécurité important
Contrairement à Organization dans immotran-ms-identity (où l'id de la ressource EST le tenant_id), ici l'id d'une propriété et son organizationId sont deux champs distincts : il faut donc toujours charger la ressource (404 si absente) avant de pouvoir vérifier l'accès au tenant (403 sinon). C'est un compromis assumé — voir les commentaires dans PropertyController.
