package cnm.prs.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.ChangePasswordRequest;
import cnm.prs.dto.LoginRequest;
import cnm.prs.dto.LoginResponse;
import cnm.prs.dto.RegisterPrmpRequest;
import cnm.prs.dto.RegisterResponse;
import cnm.prs.entity.CompteAuth;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.Prmp;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.TypeActeur;
import cnm.prs.enums.TypeNotification;
import cnm.prs.exception.BadRequestException;
import cnm.prs.exception.BusinessRuleException;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.repository.CompteAuthRepository;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.repository.ProfileRepository;
import cnm.prs.security.CurrentUser;
import cnm.prs.security.TokenService;

/**
 * Authentification : vérifie les identifiants, résout le profil et la localité, puis émet
 * un jeton JWT (§1 — visibilité par localité ; §3 — 8 profils).
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final CompteAuthRepository compteRepository;
    private final ControleurRepository controleurRepository;
    private final ProfileRepository profileRepository;
    private final PrmpRepository prmpRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ControleurDirectory controleurDirectory;
    private final NotificationService notificationService;

    public AuthService(CompteAuthRepository compteRepository, ControleurRepository controleurRepository,
            ProfileRepository profileRepository, PrmpRepository prmpRepository,
            PasswordEncoder passwordEncoder, TokenService tokenService,
            ControleurDirectory controleurDirectory, NotificationService notificationService) {
        this.compteRepository = compteRepository;
        this.controleurRepository = controleurRepository;
        this.profileRepository = profileRepository;
        this.prmpRepository = prmpRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.controleurDirectory = controleurDirectory;
        this.notificationService = notificationService;
    }

    public LoginResponse login(LoginRequest request) {
        CompteAuth compte = compteRepository.findByLogin(request.login())
                .orElseThrow(() -> new BadCredentialsException("Identifiants invalides."));
        if (!Boolean.TRUE.equals(compte.getActif())) {
            throw new BadCredentialsException("Compte désactivé.");
        }
        if (!passwordEncoder.matches(request.motDePasse(), compte.getMotDePasse())) {
            throw new BadCredentialsException("Identifiants invalides.");
        }

        TypeActeur type = parseType(compte.getTypeActeur());
        String role;
        String localite;
        if (type == TypeActeur.CONTROLEUR) {
            Controleur controleur = controleurRepository.findById(compte.getRefActeur())
                    .orElseThrow(() -> new BadCredentialsException("Contrôleur introuvable pour ce compte."));
            role = resoudreRoleControleur(controleur);
            localite = controleur.getIdLocalite(); // NULL pour le Président → toutes localités (§1.1)
        } else {
            prmpRepository.findById(compte.getRefActeur())
                    .orElseThrow(() -> new BadCredentialsException("PRMP introuvable pour ce compte."));
            role = ProfilUtilisateur.PRMP.name();
            // La PRMP n'a pas de localité propre : son périmètre est la propriété (ID_PRMP), pas la
            // localité (§1, §3.1). Le jeton ne porte donc pas de claim « localite » pour une PRMP.
            localite = null;
        }

        String token = tokenService.generer(compte.getLogin(), role, type, compte.getRefActeur(), localite);
        return new LoginResponse(token, compte.getLogin(), role, type.name(),
                compte.getRefActeur(), localite, tokenService.getExpirationSeconds());
    }

    /**
     * Auto-inscription d'une PRMP : crée la fiche {@code t_prmp} et un compte d'authentification
     * <strong>inactif</strong> (en attente de validation par l'Administrateur). Le profil n'est
     * pas demandé (le rôle PRMP découle du type d'acteur). Le login et l'identifiant PRMP doivent
     * être uniques.
     */
    @Transactional
    public RegisterResponse registerPrmp(RegisterPrmpRequest req) {
        if (compteRepository.findByLogin(req.login()).isPresent()) {
            throw new BusinessRuleException("Ce login est déjà utilisé.");
        }
        if (prmpRepository.existsById(req.idPrmp())) {
            throw new BusinessRuleException("Cette PRMP (id " + req.idPrmp() + ") est déjà enregistrée.");
        }

        Prmp prmp = new Prmp();
        prmp.setIdPrmp(req.idPrmp());
        prmp.setNomPrmp(req.nomPrmp());
        prmp.setPrenomsPrmp(req.prenomsPrmp());
        prmp.setImPrmp(req.imPrmp());
        prmp.setArreteNomin(req.arreteNomin());
        prmp.setDateNomin(req.dateNomin());
        prmp.setCin(req.cin());
        prmp.setDateCin(req.dateCin());
        prmp.setLieuCin(req.lieuCin());
        prmp.setEmailPrmp(req.emailPrmp());
        prmp.setTelPrmp(req.telPrmp());
        prmpRepository.save(prmp);

        CompteAuth compte = new CompteAuth(req.login(), passwordEncoder.encode(req.motDePasse()),
                TypeActeur.PRMP.name(), req.idPrmp(), false);
        compteRepository.save(compte);

        notifierAdministrateurs(prmp);

        return new RegisterResponse(req.login(), req.idPrmp(), TypeActeur.PRMP.name(), false,
                "Inscription enregistrée. Votre compte est en attente de validation par l'administrateur.");
    }

    /** Notifie chaque Administrateur d'une nouvelle inscription PRMP à valider. */
    private void notifierAdministrateurs(Prmp prmp) {
        String titre = "Nouvelle inscription PRMP à valider";
        String corps = "La PRMP " + prmp.getNomPrmp() + " " + prmp.getPrenomsPrmp()
                + " (id " + prmp.getIdPrmp()
                + ") s'est inscrite et attend la validation de son compte.";
        for (Controleur admin : controleurDirectory.administrateurs()) {
            notificationService.emettre(null, TypeNotification.NOUVELLE_INSCRIPTION,
                    admin.getImControleur(), admin.getEmailCont(), titre, corps);
        }
    }

    /**
     * Change le mot de passe de l'utilisateur authentifié, après vérification de l'ancien.
     * Le nouveau doit différer de l'actuel.
     */
    @Transactional
    public void changerMotDePasse(ChangePasswordRequest req) {
        String login = CurrentUser.login()
                .orElseThrow(() -> new AccessDeniedException("Utilisateur non identifié."));
        CompteAuth compte = compteRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable : " + login));

        if (!passwordEncoder.matches(req.ancienMotDePasse(), compte.getMotDePasse())) {
            throw new BadRequestException("Le mot de passe actuel est incorrect.");
        }
        if (passwordEncoder.matches(req.nouveauMotDePasse(), compte.getMotDePasse())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'actuel.");
        }
        compte.setMotDePasse(passwordEncoder.encode(req.nouveauMotDePasse()));
        compteRepository.save(compte);
    }

    /** Résout le rôle d'un contrôleur via le libellé de son profil ({@code tr_profile}). */
    private String resoudreRoleControleur(Controleur controleur) {
        if (controleur.getIdProfile() == null) {
            return null;
        }
        String libelle = profileRepository.findById(controleur.getIdProfile())
                .map(p -> p.getProfile())
                .orElse(null);
        ProfilUtilisateur profil = ProfilUtilisateur.resolve(libelle);
        return profil != null ? profil.name() : null;
    }

    private TypeActeur parseType(String type) {
        try {
            return TypeActeur.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BadCredentialsException("Type d'acteur du compte invalide.");
        }
    }
}
