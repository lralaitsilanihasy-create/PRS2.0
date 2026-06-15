package cnm.prs.dto;

import java.util.List;

/**
 * Résultat d'une validation d'inscription (partielle) : entités activées, entités en conflit
 * (non activées, avec motif), et statut final du compte ({@code ACTIF} si au moins une entité a
 * été activée, sinon {@code EN_ATTENTE}).
 */
public record ValidationInscriptionResponse(
        List<String> validees,
        List<Conflit> conflits,
        String statutCompte) {

    /** Une entité non activée et sa raison (entité existante déjà prise, ou proposée non retenue). */
    public record Conflit(
            Integer idEntiteContract,
            String libelle,
            String motif) {
    }
}
