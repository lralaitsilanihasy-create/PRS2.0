package cnm.prs.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.repository.SequenceReferenceRepository;

/**
 * Génère la référence officielle d'un dossier au format
 * {@code xxxxx/type_dossier/code_localite/annee_exercice}. Le compteur xxxxx est incrémenté
 * par la base, par combinaison (type, localité, exercice) — aucun compteur applicatif.
 */
@Service
public class ReferenceService {

    private final SequenceReferenceRepository repository;

    public ReferenceService(SequenceReferenceRepository repository) {
        this.repository = repository;
    }

    /**
     * @param typeDossier   type du dossier (PPM, DAO, MAOO…)
     * @param localite      localité du dossier (utilisée seulement si non centrale)
     * @param estCentrale   true -> segment = "CNM" ; false -> "CRM-" + localite
     * @param anneeExercice exercice budgétaire
     */
    @Transactional
    public String generer(String typeDossier, String localite, boolean estCentrale, int anneeExercice) {
        String codeLocalite = estCentrale ? "CNM" : "CRM-" + localite;
        // Incrément côté SGBD : UPDATE +1 si le contexte existe, sinon création à 1 (portable H2/Postgres).
        if (repository.incrementerExistant(typeDossier, codeLocalite, anneeExercice) == 0) {
            repository.creer(typeDossier, codeLocalite, anneeExercice);
        }
        long valeur = repository.valeurCourante(typeDossier, codeLocalite, anneeExercice);
        return String.format("%05d/%s/%s/%d", valeur, typeDossier, codeLocalite, anneeExercice);
    }
}
