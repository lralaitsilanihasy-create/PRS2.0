package cnm.prs.exception;

import java.util.List;

/**
 * Levée pour une validation métier par champ (hors {@code @Valid}) : traduite en HTTP 400 avec le
 * détail {@code erreurs:[{champ,message}]}. Utilisée quand le nom de champ attendu côté contrat
 * diffère du chemin que produirait le bean-validation (ex. {@code dateDebut} vs {@code marches[0].dateDebut}).
 */
public class ChampsInvalidesException extends RuntimeException {

    private final transient List<ErrorResponse.FieldError> erreurs;

    public ChampsInvalidesException(List<ErrorResponse.FieldError> erreurs) {
        super("Validation échouée");
        this.erreurs = erreurs;
    }

    public List<ErrorResponse.FieldError> getErreurs() {
        return erreurs;
    }
}
