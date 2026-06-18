package cnm.prs.dto;

/**
 * Un mode de passation autorisé pour des paramètres (situation, nature, montant, localité)
 * donnés : son identifiant et son libellé ({@code tr_mode_passation.LIBELLE}).
 */
public record ModeAutorise(Integer idMode, String libelle) {
}
