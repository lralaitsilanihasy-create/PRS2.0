package cnm.prs.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import cnm.prs.exception.ErrorResponse;

/**
 * ⚠️ Règle ajoutée — cohérence chronologique des processus (CAPM) des dates prévisionnelles d'un
 * marché. Deux règles, sur les processus <strong>triés par ordre CAPM ASC</strong> :
 * <ol>
 *   <li>cohérence interne : {@code dateDebut < dateFin} de chaque processus ;</li>
 *   <li>cohérence entre processus consécutifs : {@code dateDebut[n] >= dateFin[n-1]}.</li>
 * </ol>
 * Le {@code champ} de chaque processus porte le préfixe du chemin (ex. {@code marches[2].processus[0]}
 * à la saisie, ou chaîne vide à l'édition → chemin = nom du champ seul).
 */
final class ProcessusChronologie {

    private ProcessusChronologie() {
    }

    /** Un processus à valider : préfixe de chemin, ordre CAPM (tri), libellé, dates. */
    record Proc(String champ, int ordre, String libelle, LocalDate dateDebut, LocalDate dateFin) {
    }

    /**
     * Première violation chronologique (règle 1 puis règle 2) sur les processus, ou {@code null} si OK.
     * Les processus sont triés par {@code ordre} ASC avant validation.
     */
    static ErrorResponse.FieldError premiereViolation(List<Proc> processus) {
        List<Proc> tries = processus.stream().sorted(Comparator.comparingInt(Proc::ordre)).toList();

        // Règle 1 — cohérence interne de chaque processus.
        for (Proc p : tries) {
            if (p.dateDebut() != null && p.dateFin() != null && !p.dateDebut().isBefore(p.dateFin())) {
                return new ErrorResponse.FieldError(champ(p, "dateFin"),
                        "La date de fin doit être postérieure à la date de début.");
            }
        }
        // Règle 2 — cohérence entre processus consécutifs.
        for (int n = 1; n < tries.size(); n++) {
            Proc prec = tries.get(n - 1);
            Proc cur = tries.get(n);
            if (cur.dateDebut() != null && prec.dateFin() != null && cur.dateDebut().isBefore(prec.dateFin())) {
                return new ErrorResponse.FieldError(champ(cur, "dateDebut"),
                        "La date de début du processus " + cur.libelle()
                                + " doit être postérieure ou égale à la date de fin du processus précédent "
                                + prec.libelle() + ".");
            }
        }
        return null;
    }

    private static String champ(Proc p, String field) {
        return p.champ() == null || p.champ().isEmpty() ? field : p.champ() + "." + field;
    }
}
