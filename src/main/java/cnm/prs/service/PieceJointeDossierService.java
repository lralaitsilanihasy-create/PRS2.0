package cnm.prs.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cnm.prs.dto.PieceJointeDossierDto;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.PieceJointeDossier;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.StatutDossier;
import cnm.prs.exception.BadRequestException;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.mapper.PieceJointeDossierMapper;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.PieceJointeDossierRepository;
import cnm.prs.repository.TypePieceJointeRepository;
import cnm.prs.security.CurrentUser;

/**
 * Logique métier pour {@link PieceJointeDossier} : pièces jointes d'un dossier. Upload multipart
 * validé par magic-bytes (PDF/JPEG/PNG) ; {@code apresLettreRenvoi} distingue les pièces ajoutées
 * après réception d'une lettre de renvoi (dossier SOUMIS/PRET_DISPATCH + {@code idLettre}).
 */
@Service
@Transactional
public class PieceJointeDossierService {

    private final PieceJointeDossierRepository repository;
    private final TypePieceJointeRepository typePieceRepository;
    private final DossierRepository dossierRepository;

    public PieceJointeDossierService(PieceJointeDossierRepository repository,
            TypePieceJointeRepository typePieceRepository, DossierRepository dossierRepository) {
        this.repository = repository;
        this.typePieceRepository = typePieceRepository;
        this.dossierRepository = dossierRepository;
    }

    @Transactional(readOnly = true)
    public List<PieceJointeDossierDto> findByDossier(Integer idDossier) {
        return repository.findByIdDossier(idDossier).stream().map(this::toDtoAvecLibelle).toList();
    }

    @Transactional(readOnly = true)
    public PieceJointeDossierDto findById(Integer id) {
        return toDtoAvecLibelle(exigerExistante(id));
    }

    /** Pièce complète (contenu + format) pour téléchargement. */
    @Transactional(readOnly = true)
    public PieceJointeDossier telecharger(Integer id) {
        return exigerExistante(id);
    }

    /**
     * Upload d'une pièce par la PRMP propriétaire. {@code apresLettreRenvoi=true} si {@code idLettre}
     * est fourni et le dossier est SOUMIS/PRET_DISPATCH ; sinon pièce initiale (false).
     */
    public PieceJointeDossierDto store(PieceJointeDossierDto meta, MultipartFile fichier) {
        Dossier dossier = dossierRepository.findById(meta.getIdDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + meta.getIdDossier()));
        exigerProprietaire(dossier);
        boolean apres = false;
        Integer idLettre = null;
        if (meta.getIdLettre() != null
                && (StatutDossier.SOUMIS.name().equals(dossier.getStatut())
                        || StatutDossier.PRET_DISPATCH.name().equals(dossier.getStatut()))) {
            apres = true;
            idLettre = meta.getIdLettre();
        }
        return toDtoAvecLibelle(enregistrer(meta.getIdDossier(), meta.getIdTypePiece(), fichier, apres, idLettre));
    }

    /** Enregistrement d'une pièce initiale (à la saisie) : {@code apresLettreRenvoi=false}. */
    public PieceJointeDossier enregistrerInitiale(Integer idDossier, Integer idTypePiece, MultipartFile fichier) {
        return enregistrer(idDossier, idTypePiece, fichier, false, null);
    }

    private PieceJointeDossier enregistrer(Integer idDossier, Integer idTypePiece, MultipartFile fichier,
            boolean apresLettreRenvoi, Integer idLettre) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BadRequestException("Pièce jointe manquante ou vide.");
        }
        byte[] contenu = lire(fichier);
        String format = detecterFormat(contenu);
        if (format == null) {
            throw new BadRequestException("Type de fichier non autorisé : seuls PDF, JPEG et PNG sont acceptés.");
        }
        PieceJointeDossier p = new PieceJointeDossier();
        p.setIdDossier(idDossier);
        p.setIdTypePiece(idTypePiece);
        p.setNomFichier(fichier.getOriginalFilename());
        p.setContenu(contenu);
        p.setFormat(format);
        p.setTaille((long) contenu.length);
        p.setDateUpload(LocalDateTime.now());
        p.setApresLettreRenvoi(apresLettreRenvoi);
        p.setIdLettre(idLettre);
        return repository.save(p);
    }

    /** Suppression : Administrateur, ou PRMP propriétaire d'un dossier encore BROUILLON. */
    public void delete(Integer id) {
        PieceJointeDossier piece = exigerExistante(id);
        if (CurrentUser.profil().orElse(null) == ProfilUtilisateur.ADMINISTRATEUR) {
            repository.deleteById(id);
            return;
        }
        Dossier dossier = dossierRepository.findById(piece.getIdDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + piece.getIdDossier()));
        exigerProprietaire(dossier);
        if (!StatutDossier.BROUILLON.name().equals(dossier.getStatut())) {
            throw new BusinessRuleException("Suppression possible uniquement tant que le dossier est BROUILLON.");
        }
        repository.deleteById(id);
    }

    private PieceJointeDossier exigerExistante(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe introuvable : " + id));
    }

    /** Propriété : seule la PRMP propriétaire du dossier peut déposer/supprimer ses pièces. */
    private void exigerProprietaire(Dossier dossier) {
        String moi = CurrentUser.ref().orElse(null);
        if (dossier.getIdPrmp() == null || !dossier.getIdPrmp().equals(moi)) {
            throw new AccessDeniedException("Pièce réservée à la PRMP propriétaire du dossier.");
        }
    }

    private PieceJointeDossierDto toDtoAvecLibelle(PieceJointeDossier entity) {
        PieceJointeDossierDto dto = PieceJointeDossierMapper.toDto(entity);
        if (dto != null && dto.getIdTypePiece() != null) {
            typePieceRepository.findById(dto.getIdTypePiece())
                    .ifPresent(t -> dto.setLibellePiece(t.getLibellePiece()));
        }
        return dto;
    }

    private byte[] lire(MultipartFile fichier) {
        try {
            return fichier.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Lecture du fichier impossible : " + e.getMessage());
        }
    }

    /** Magic-bytes : renvoie {@code PDF}/{@code JPEG}/{@code PNG} ou {@code null} si non reconnu. */
    private String detecterFormat(byte[] d) {
        if (d.length >= 4 && d[0] == 0x25 && d[1] == 0x50 && d[2] == 0x44 && d[3] == 0x46) {
            return "PDF"; // %PDF
        }
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "JPEG";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 0x50 && d[2] == 0x4E && d[3] == 0x47
                && d[4] == 0x0D && d[5] == 0x0A && d[6] == 0x1A && d[7] == 0x0A) {
            return "PNG";
        }
        return null;
    }
}
