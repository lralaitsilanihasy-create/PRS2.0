package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu Assistant contrôleur — filtrés sur sa localité : les
 * documents signés qu'il distribue.
 *
 * @param lettresRenvoi lettres de renvoi signées de sa localité ({@code t_lettre_renvoi.STATUT = SIGNE})
 * @param pvDefinitifs  PV définitifs (signés) de sa localité ({@code t_pv_examen.STATUT_PV = SIGNE})
 */
public record CompteursAssistantDto(
        long lettresRenvoi,
        long pvDefinitifs) {
}
