package cnm.prs.service;

import java.text.Normalizer;
import java.util.Set;

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

    private static final Set<String> MOTS_VIDES =
            Set.of("de", "du", "des", "la", "le", "les", "l", "d", "et", "a", "aux", "en", "pour", "sur", "par");

    /**
     * ⚠️ Règle ajoutée — référence PPM dérivée du libellé de l'entité :
     * {@code <seq>/<acronyme>/PPM/<année>}. Compteur par (acronyme entité, année) via la table générique
     * {@code t_sequence_reference} (clé {@code (PPM_REF, acronyme, année)} — distincte des réf. de réception).
     */
    @Transactional
    public String genererPpm(String libelleEntite, int annee) {
        String code = acronymeEntite(libelleEntite);
        if (repository.incrementerExistant("PPM_REF", code, annee) == 0) {
            repository.creer("PPM_REF", code, annee);
        }
        long valeur = repository.valeurCourante("PPM_REF", code, annee);
        return String.format("%05d/%s/PPM/%d", valeur, code, annee);
    }

    /** Acronyme = initiales (sans accent) des mots significatifs du libellé. « Direction Générale du Budget » → « DGB ». */
    private String acronymeEntite(String libelle) {
        if (libelle == null || libelle.isBlank()) {
            return "ENT";
        }
        StringBuilder sb = new StringBuilder();
        for (String mot : libelle.trim().split("\\s+")) {
            String lettres = Normalizer.normalize(mot, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "").replaceAll("[^A-Za-z]", "");
            if (lettres.isEmpty() || MOTS_VIDES.contains(lettres.toLowerCase())) {
                continue;
            }
            sb.append(Character.toUpperCase(lettres.charAt(0)));
        }
        return sb.length() == 0 ? "ENT" : sb.toString();
    }
}
