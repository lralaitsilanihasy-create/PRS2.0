package cnm.prs.dto;

/**
 * Compteurs de contenu par section du menu Membre — filtrés sur le Membre attributaire (son IM,
 * via {@code Dispatch.imCtrlMembre}), miroir de ses deux worklists ({@code /api/dossiers/a-examiner},
 * {@code /examines}).
 *
 * @param aExaminer dossiers à examiner ({@code DISPATCHE} qui lui sont attribués)
 * @param examines  dossiers examinés ({@code EXAMINE}/{@code PV_SIGNE}/{@code EN_VERIFICATION}/{@code CLOTURE})
 */
public record CompteursMembreDto(
        long aExaminer,
        long examines) {
}
