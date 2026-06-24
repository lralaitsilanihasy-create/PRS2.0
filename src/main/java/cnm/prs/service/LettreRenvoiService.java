package cnm.prs.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.LettreRenvoiDto;
import cnm.prs.entity.LettreRenvoi;
import cnm.prs.enums.StatutLettreRenvoi;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.LettreRenvoiMapper;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.repository.LettreRenvoiRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.Visibilite;

/**
 * Logique métier pour {@link LettreRenvoi} (alternative au Projet de PV).
 * Circuit : {@code BROUILLON → SOUMIS → SIGNE} ; signature par le CC ou le Président uniquement.
 */
@Service
@Transactional
public class LettreRenvoiService {

    private final LettreRenvoiRepository repository;
    private final ExamenRepository examenRepository;

    public LettreRenvoiService(LettreRenvoiRepository repository, ExamenRepository examenRepository) {
        this.repository = repository;
        this.examenRepository = examenRepository;
    }

    @Transactional(readOnly = true)
    public List<LettreRenvoiDto> findAll() {
        return Visibilite.filtrer(repository::findAll, repository::findVisiblesParLocalite)
                .stream().map(LettreRenvoiMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public LettreRenvoiDto findById(Integer id) {
        LettreRenvoi entity = exigerExistante(id);
        Visibilite.controler(loc -> repository.existsDansLocalite(id, loc));
        return LettreRenvoiMapper.toDto(entity);
    }

    /**
     * Crée la lettre de renvoi d'un examen (statut BROUILLON). {@code refLettre} dérivée de
     * {@code refeDossier} (insertion de {@code /LR/} avant l'année) ; {@code idDossier} et
     * {@code dateExamen} dérivés de l'examen ; {@code dateLettre} = jour. Un examen → au plus une lettre.
     */
    public LettreRenvoiDto creerDepuisExamen(Integer idExamen, String objetLettre) {
        if (repository.existsByIdExamen(idExamen)) {
            throw new BusinessRuleException("Une lettre de renvoi existe déjà pour cet examen.");
        }
        LettreRenvoi lettre = new LettreRenvoi();
        lettre.setIdExamen(idExamen);
        lettre.setIdDossier(examenRepository.findIdDossierByExamen(idExamen).orElse(null));
        lettre.setObjetLettre(objetLettre);
        lettre.setRefLettre(genererRefLettre(idExamen));
        lettre.setDateExamen(examenRepository.findById(idExamen).map(e -> e.getDateExamen()).orElse(null));
        lettre.setDateLettre(LocalDate.now());
        lettre.setStatut(StatutLettreRenvoi.BROUILLON.name());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    /** Référence dérivée de refeDossier {@code .../YYYY} → {@code .../LR/YYYY} (sinon null). */
    private String genererRefLettre(Integer idExamen) {
        String refe = examenRepository.findRefeDossierByExamen(idExamen)
                .filter(s -> s != null && s.matches(".*/\\d{4}$")).orElse(null);
        return refe == null ? null : refe.replaceFirst("/(\\d{4})$", "/LR/$1");
    }

    /** Édition du brouillon (objet de la lettre) par le Membre. */
    public LettreRenvoiDto update(Integer id, LettreRenvoiDto dto) {
        LettreRenvoi lettre = exigerExistante(id);
        if (!StatutLettreRenvoi.BROUILLON.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Lettre non éditable : statut « " + lettre.getStatut() + " » (attendu BROUILLON).");
        }
        lettre.setObjetLettre(dto.getObjetLettre());
        lettre.setCorpsLettre(dto.getCorpsLettre());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    /** Soumission par le Membre propriétaire (attributaire de l'examen) : BROUILLON → SOUMIS. */
    public LettreRenvoiDto soumettre(Integer id) {
        LettreRenvoi lettre = exigerExistante(id);
        exigerProprietaire(lettre);
        if (!StatutLettreRenvoi.BROUILLON.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Soumission impossible : statut « " + lettre.getStatut() + " » (attendu BROUILLON).");
        }
        lettre.setStatut(StatutLettreRenvoi.SOUMIS.name());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    /** Signature par le CC ou le Président (rôle contrôlé au contrôleur) : SOUMIS → SIGNE ; imSignataire = JWT. */
    public LettreRenvoiDto signer(Integer id) {
        LettreRenvoi lettre = exigerExistante(id);
        if (!StatutLettreRenvoi.SOUMIS.name().equals(lettre.getStatut())) {
            throw new BusinessRuleException("Signature impossible : statut « " + lettre.getStatut() + " » (attendu SOUMIS).");
        }
        lettre.setImSignataire(CurrentUser.ref().filter(s -> !s.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Signataire non identifié.")));
        lettre.setStatut(StatutLettreRenvoi.SIGNE.name());
        return LettreRenvoiMapper.toDto(repository.save(lettre));
    }

    public void delete(Integer id) {
        exigerExistante(id);
        repository.deleteById(id);
    }

    private LettreRenvoi exigerExistante(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lettre de renvoi introuvable : " + id));
    }

    /** Propriété (§2.4) : seul le Membre attributaire de l'examen (Examen.imCtrlMembre) peut soumettre. */
    private void exigerProprietaire(LettreRenvoi lettre) {
        String attributaire = examenRepository.findById(lettre.getIdExamen())
                .map(e -> e.getImCtrlMembre()).orElse(null);
        String moi = CurrentUser.ref().orElse(null);
        if (attributaire == null || !attributaire.equals(moi)) {
            throw new AccessDeniedException("Lettre réservée au Membre attributaire de l'examen.");
        }
    }
}
