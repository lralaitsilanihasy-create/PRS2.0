package cnm.prs.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.entity.Controleur;
import cnm.prs.entity.Profile;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.ProfileRepository;

/**
 * Annuaire des contrôleurs par profil métier. Résout le(s) {@code ID_PROFILE} d'un
 * {@link ProfilUtilisateur} à partir du libellé {@code tr_profile.PROFILE} (même logique
 * que l'authentification), puis retourne les contrôleurs correspondants. Utilisé pour
 * adresser les notifications (Président, Chef de commission d'une localité…).
 */
@Component
@Transactional(readOnly = true)
public class ControleurDirectory {

    private final ProfileRepository profileRepository;
    private final ControleurRepository controleurRepository;

    public ControleurDirectory(ProfileRepository profileRepository, ControleurRepository controleurRepository) {
        this.profileRepository = profileRepository;
        this.controleurRepository = controleurRepository;
    }

    /** Tous les Présidents (visibilité toutes localités). */
    public List<Controleur> presidents() {
        return parProfil(ProfilUtilisateur.PRESIDENT);
    }

    /** Tous les Chargés de publication. */
    public List<Controleur> chargesPublication() {
        return parProfil(ProfilUtilisateur.CHARGE_PUBLICATION);
    }

    /** Tous les Administrateurs. */
    public List<Controleur> administrateurs() {
        return parProfil(ProfilUtilisateur.ADMINISTRATEUR);
    }

    /** Les Chefs de commission d'une localité donnée. */
    public List<Controleur> chefsCommission(String idLocalite) {
        List<Integer> ids = idProfiles(ProfilUtilisateur.CHEF_COMMISSION);
        if (ids.isEmpty() || idLocalite == null) {
            return List.of();
        }
        return controleurRepository.findByIdProfileInAndIdLocalite(ids, idLocalite);
    }

    /** Les Secrétaires d'une localité donnée (réception des dossiers, §3.4). */
    public List<Controleur> secretaires(String idLocalite) {
        List<Integer> ids = idProfiles(ProfilUtilisateur.SECRETAIRE);
        if (ids.isEmpty() || idLocalite == null) {
            return List.of();
        }
        return controleurRepository.findByIdProfileInAndIdLocalite(ids, idLocalite);
    }

    /** Les Contrôleurs vérificateurs d'une localité donnée (§3.6, transmission du PV signé). */
    public List<Controleur> verificateurs(String idLocalite) {
        List<Integer> ids = idProfiles(ProfilUtilisateur.VERIFICATEUR);
        if (ids.isEmpty() || idLocalite == null) {
            return List.of();
        }
        return controleurRepository.findByIdProfileInAndIdLocalite(ids, idLocalite);
    }

    private List<Controleur> parProfil(ProfilUtilisateur profil) {
        List<Integer> ids = idProfiles(profil);
        return ids.isEmpty() ? List.of() : controleurRepository.findByIdProfileIn(ids);
    }

    private List<Integer> idProfiles(ProfilUtilisateur profil) {
        return profileRepository.findAll().stream()
                .filter(p -> ProfilUtilisateur.resolve(p.getProfile()) == profil)
                .map(Profile::getIdProfile)
                .toList();
    }
}
