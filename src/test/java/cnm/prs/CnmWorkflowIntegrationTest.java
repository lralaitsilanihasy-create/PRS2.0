package cnm.prs;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.PieceJointeMetaDto;
import cnm.prs.entity.Avis;
import cnm.prs.entity.CompteAuth;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.DelegationProfil;
import cnm.prs.entity.DemandeRetrait;
import cnm.prs.entity.Dispatch;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Examen;
import cnm.prs.entity.Localite;
import cnm.prs.entity.Marche;
import cnm.prs.entity.ModePassation;
import cnm.prs.entity.Nature;
import cnm.prs.entity.Ppm;
import cnm.prs.entity.Prmp;
import cnm.prs.entity.Profile;
import cnm.prs.entity.Reception;
import cnm.prs.entity.ReglePassation;
import cnm.prs.entity.Seuil;
import cnm.prs.entity.EntiteContract;
import cnm.prs.entity.Ministere;
import cnm.prs.entity.Organigramme;
import cnm.prs.entity.PrmpEntite;
import cnm.prs.entity.Situation;
import cnm.prs.entity.TypeDossier;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.TypeActeur;
import cnm.prs.enums.TypePieceJointe;
import cnm.prs.exception.BadRequestException;
import cnm.prs.repository.AvisRepository;
import cnm.prs.repository.CompteAuthRepository;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.DelegationProfilRepository;
import cnm.prs.repository.DemandeRetraitRepository;
import cnm.prs.repository.DispatchRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.repository.LocaliteRepository;
import cnm.prs.repository.MarcheRepository;
import cnm.prs.repository.ModePassationRepository;
import cnm.prs.repository.NatureRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.repository.ProfileRepository;
import cnm.prs.repository.ReceptionRepository;
import cnm.prs.repository.ReglePassationRepository;
import cnm.prs.repository.SeuilRepository;
import cnm.prs.repository.EntiteContractRepository;
import cnm.prs.repository.MinistereRepository;
import cnm.prs.repository.OrganigrammeRepository;
import cnm.prs.repository.PrmpEntiteRepository;
import cnm.prs.repository.SituationRepository;
import cnm.prs.repository.TypeDossierRepository;
import cnm.prs.security.TokenService;
import cnm.prs.service.PieceJointeService;

/**
 * Tests d'intégration de bout en bout : authentification JWT, autorisations par profil,
 * workflow du PV et comportements automatiques. Exécutés sur une base H2 isolée
 * (cf. src/test/resources/application.properties), chaque test étant transactionnel et
 * annulé en fin d'exécution.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CnmWorkflowIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private TokenService tokenService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PieceJointeService pieceJointeService;

    @Autowired private LocaliteRepository localiteRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private ControleurRepository controleurRepository;
    @Autowired private PrmpRepository prmpRepository;
    @Autowired private CompteAuthRepository compteAuthRepository;
    @Autowired private AvisRepository avisRepository;
    @Autowired private DossierRepository dossierRepository;
    @Autowired private ReceptionRepository receptionRepository;
    @Autowired private DispatchRepository dispatchRepository;
    @Autowired private ExamenRepository examenRepository;
    @Autowired private PpmRepository ppmRepository;
    @Autowired private MarcheRepository marcheRepository;
    @Autowired private DemandeRetraitRepository demandeRetraitRepository;
    @Autowired private DelegationProfilRepository delegationProfilRepository;
    @Autowired private NatureRepository natureRepository;
    @Autowired private SituationRepository situationRepository;
    @Autowired private ModePassationRepository modePassationRepository;
    @Autowired private SeuilRepository seuilRepository;
    @Autowired private ReglePassationRepository reglePassationRepository;
    @Autowired private TypeDossierRepository typeDossierRepository;
    @Autowired private MinistereRepository ministereRepository;
    @Autowired private OrganigrammeRepository organigrammeRepository;
    @Autowired private EntiteContractRepository entiteContractRepository;
    @Autowired private PrmpEntiteRepository prmpEntiteRepository;

    private String tokenPresident;
    private String tokenCc;
    private String tokenMembre;
    private String tokenAdmin;
    private String tokenPrmp;
    private String tokenPublication;

    @BeforeEach
    void seed() {
        localiteRepository.save(localite("ANT", "Antananarivo"));
        typeDossierRepository.save(new TypeDossier("PPM", "Plan de passation des marchés"));
        typeDossierRepository.save(new TypeDossier("DAO", "Dossier d'appel d'offres"));

        profileRepository.save(profile(1, "PRMP"));
        profileRepository.save(profile(2, "Président"));
        profileRepository.save(profile(3, "Chef de commission"));
        profileRepository.save(profile(4, "Secrétaire"));
        profileRepository.save(profile(5, "Membre"));
        profileRepository.save(profile(6, "Contrôleur vérificateur"));
        profileRepository.save(profile(7, "Chargé de publication"));
        profileRepository.save(profile(8, "Administrateur"));

        controleurRepository.save(controleur("CTRPRE", 2, null));   // Président, voit tout
        controleurRepository.save(controleur("CTRCC1", 3, "ANT"));  // Chef de commission
        controleurRepository.save(controleur("CTRSEC", 4, "ANT"));  // Secrétaire
        controleurRepository.save(controleur("CTRMEM", 5, "ANT"));  // Membre
        controleurRepository.save(controleur("CTRADM", 8, "ANT"));  // Administrateur
        controleurRepository.save(controleur("CTRPUB", 7, null));   // Chargé de publication
        prmpRepository.save(prmp("PRMP001", "ANT"));

        // Délégations actives (orientation MLD : délégant = profil qui exerce,
        // délégué = profil dont la tâche est exercée). Président (2) et CC (3) exercent
        // les tâches de Secrétaire (4), Membre (5) et Vérificateur (6).
        delegationProfilRepository.save(delegation(1, 2, 4));
        delegationProfilRepository.save(delegation(2, 2, 5));
        delegationProfilRepository.save(delegation(3, 2, 6));
        delegationProfilRepository.save(delegation(4, 3, 4));
        delegationProfilRepository.save(delegation(5, 3, 5));
        delegationProfilRepository.save(delegation(6, 3, 6));

        String hash = passwordEncoder.encode("pw");
        compteAuthRepository.save(new CompteAuth("CTRPRE", hash, "CONTROLEUR", "CTRPRE", true));
        compteAuthRepository.save(new CompteAuth("CTRCC1", hash, "CONTROLEUR", "CTRCC1", true));
        compteAuthRepository.save(new CompteAuth("CTRMEM", hash, "CONTROLEUR", "CTRMEM", true));
        compteAuthRepository.save(new CompteAuth("CTRADM", hash, "CONTROLEUR", "CTRADM", true));
        compteAuthRepository.save(new CompteAuth("PRMP001", hash, "PRMP", "PRMP001", true));

        // Circuit amont pour le workflow PV.
        avisRepository.save(avis("FAV", "Favorable"));
        dossierRepository.save(dossier(1, "EN_EXAMEN"));
        receptionRepository.save(reception(1, 1, "CTRCC1", false));
        dispatchRepository.save(dispatch(1, 1, "CTRCC1", "CTRMEM"));
        examenRepository.save(examen(1, 1, "CTRMEM"));
        ppmRepository.save(ppm(1, 1, "PRMP001")); // PPM du dossier 1 appartenant à PRMP001

        // Seconde localité (TMS) : un CC, un dossier et sa réception — pour la règle d'intérim.
        localiteRepository.save(localite("TMS", "Toamasina"));
        controleurRepository.save(controleur("CTRCC2", 3, "TMS"));
        dossierRepository.save(dossier(2, "EN_EXAMEN"));
        receptionRepository.save(reception(2, 2, "CTRCC2", false));

        // Une demande de retrait de PRMP001 sur le dossier 1 (localité ANT).
        demandeRetraitRepository.save(demandeRetrait(1, 1, "PRMP001"));

        // Entités contractantes localisées + affectations de PRMP001 (entité 1 = ANT, entité 2 = TMS).
        // La localité d'un dossier saisi est dérivée de l'entité choisie.
        ministereRepository.save(ministere(1));
        organigrammeRepository.save(organigramme(1, 1));
        entiteContractRepository.save(entite(1, 1, "ANT"));
        entiteContractRepository.save(entite(2, 1, "TMS"));
        prmpEntiteRepository.save(prmpEntite(1, "PRMP001", 1, true));
        prmpEntiteRepository.save(prmpEntite(2, "PRMP001", 2, true));

        tokenPresident = bearer("CTRPRE", ProfilUtilisateur.PRESIDENT, TypeActeur.CONTROLEUR, "CTRPRE", null);
        tokenCc = bearer("CTRCC1", ProfilUtilisateur.CHEF_COMMISSION, TypeActeur.CONTROLEUR, "CTRCC1", "ANT");
        tokenMembre = bearer("CTRMEM", ProfilUtilisateur.MEMBRE, TypeActeur.CONTROLEUR, "CTRMEM", "ANT");
        tokenAdmin = bearer("CTRADM", ProfilUtilisateur.ADMINISTRATEUR, TypeActeur.CONTROLEUR, "CTRADM", "ANT");
        tokenPrmp = bearer("PRMP001", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMP001", "ANT");
        tokenPublication = bearer("CTRPUB", ProfilUtilisateur.CHARGE_PUBLICATION, TypeActeur.CONTROLEUR, "CTRPUB", null);
    }

    // ------------------------------------------------------------------
    // Détermination automatique du mode de passation (§3.1, Module 02)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Mode de passation auto : create impose le mode, update recalcule, sans règle → null + alerte, localité PRMP absente → 400")
    void determinationAutomatiqueModePassation() throws Exception {
        // Référentiels : natures, situations, modes.
        natureRepository.save(new Nature(1, "Travaux", null));
        situationRepository.save(new Situation(1, "Normale", null));
        situationRepository.save(new Situation(2, "Urgence", null));
        modePassationRepository.save(new ModePassation(1, "AOO", null, null, null, null));
        modePassationRepository.save(new ModePassation(2, "AOR", null, null, null, null));
        modePassationRepository.save(new ModePassation(3, "Gré à gré", null, null, null, null));
        // Seuils ANT / Travaux (tranches de montant) et règles (situation, seuil) → mode.
        seuilRepository.save(seuil(902, "ANT", 1, "200000001", "1000000000"));
        seuilRepository.save(seuil(903, "ANT", 1, "1000000001", null));
        reglePassationRepository.save(regle(902, 1, 902, 2)); // normale, 200M–1Md → AOR
        reglePassationRepository.save(regle(908, 2, 902, 3)); // urgence, 200M–1Md → Gré à gré
        reglePassationRepository.save(regle(903, 1, 903, 1)); // normale, >1Md → AOO

        // Brouillon PPM propriété de PRMP001 + son PPM (les lignes de marché sont saisies par la PRMP).
        Dossier dPpm = dossier(50, "BROUILLON");
        dPpm.setIdTypeDossier("PPM");
        dPpm.setIdPrmp("PRMP001");
        dPpm.setIdLocalite("ANT");
        dossierRepository.save(dPpm);
        ppmRepository.save(ppm(50, 50, "PRMP001"));   // idPrmp PRMP001 → localité ANT pour le mode

        String tok = tokenPrmp;

        // 1) Création : mode imposé = 2 (AOR). Le idMode=99 envoyé par le client est ignoré.
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7001,\"idDossier\":50,\"idPpm\":50,\"montEstim\":500000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"idMode\":99,\"statut\":\"PREVU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMode").value(2));

        // 2) Situation = urgence → mode 3 (Gré à gré), même nature/montant/localité.
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7002,\"idDossier\":50,\"idPpm\":50,\"montEstim\":500000000,"
                        + "\"idNature\":1,\"idSituation\":2,\"statut\":\"PREVU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMode").value(3));

        // 3) Montant hors de toute tranche → aucune règle → idMode null + alerte MODE_NON_DETERMINE.
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7003,\"idDossier\":50,\"idPpm\":50,\"montEstim\":100000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMode").value(nullValue()));
        mvc.perform(get("/api/notifications").header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$[?(@.typeNotif=='MODE_NON_DETERMINE')]", hasSize(1)));

        // 4) Mise à jour : le montant passe à 1,5 Md → recalcul → mode 1 (AOO).
        mvc.perform(put("/api/marches/7001").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":50,\"idPpm\":50,\"montEstim\":1500000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMode").value(1));

        // 5) Dossier sans localité → mode indéterminable → refus 400.
        Dossier sansLoc = dossier(51, "BROUILLON");
        sansLoc.setIdTypeDossier("PPM");
        sansLoc.setIdPrmp("PRMP001");
        sansLoc.setIdLocalite(null);
        dossierRepository.save(sansLoc);
        ppmRepository.save(ppm(51, 51, "PRMP001"));
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7004,\"idDossier\":51,\"idPpm\":51,\"montEstim\":500000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}"))
                .andExpect(status().isBadRequest());
    }

    private static Seuil seuil(Integer id, String localite, Integer nature, String min, String max) {
        Seuil s = new Seuil();
        s.setIdSeuil(id);
        s.setIdLocalite(localite);
        s.setIdNature(nature);
        s.setMontantMin(min == null ? null : new BigDecimal(min));
        s.setMontantMax(max == null ? null : new BigDecimal(max));
        return s;
    }

    private static ReglePassation regle(Integer id, Integer situation, Integer seuil, Integer mode) {
        ReglePassation r = new ReglePassation();
        r.setIdRegle(id);
        r.setIdSituation(situation);
        r.setIdSeuil(seuil);
        r.setIdMode(mode);
        r.setPriorite(1);
        return r;
    }

    // ------------------------------------------------------------------
    // Authentification
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Réinitialisation Admin : l'Admin force un nouveau mot de passe")
    void adminReinitialiseMotDePasse() throws Exception {
        String body = "{\"nouveauMotDePasse\":\"Reinit#2026\"}";
        // Un non-admin ne peut pas réinitialiser → 403.
        mvc.perform(post("/api/comptes-auth/CTRMEM/reinitialiser-mot-de-passe").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        // L'Admin réinitialise le mot de passe de CTRMEM.
        mvc.perform(post("/api/comptes-auth/CTRMEM/reinitialiser-mot-de-passe").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        // CTRMEM se connecte avec le nouveau mot de passe.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"CTRMEM\",\"motDePasse\":\"Reinit#2026\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("MEMBRE"));
        // Compte inexistant → 404.
        mvc.perform(post("/api/comptes-auth/INCONNU/reinitialiser-mot-de-passe").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Changer son mot de passe : nouveau mdp actif, ancien rejeté, garde du mdp actuel")
    void changerMotDePasse() throws Exception {
        // Changement réussi (le mot de passe seedé est « pw »).
        mvc.perform(post("/api/mon-compte/changer-mot-de-passe").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ancienMotDePasse\":\"pw\",\"nouveauMotDePasse\":\"Nouveau#2026\"}"))
                .andExpect(status().isOk());

        // Connexion avec le nouveau mot de passe → OK.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"CTRMEM\",\"motDePasse\":\"Nouveau#2026\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("MEMBRE"));

        // Connexion avec l'ancien mot de passe → refusée.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"CTRMEM\",\"motDePasse\":\"pw\"}"))
                .andExpect(status().isUnauthorized());

        // Mauvais mot de passe actuel → 400.
        mvc.perform(post("/api/mon-compte/changer-mot-de-passe").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ancienMotDePasse\":\"FAUX\",\"nouveauMotDePasse\":\"Encore#2026\"}"))
                .andExpect(status().isBadRequest());

        // Sans jeton → 401.
        mvc.perform(post("/api/mon-compte/changer-mot-de-passe").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ancienMotDePasse\":\"pw\",\"nouveauMotDePasse\":\"Autre#2026\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auto-inscription PRMP : compte inactif → activation Admin → connexion")
    void autoInscriptionPrmp_validationAdmin() throws Exception {
        String inscription = "{"
                + "\"login\":\"prmp.new\",\"motDePasse\":\"Passw0rd!\",\"idPrmp\":\"PRMP777\","
                + "\"nomPrmp\":\"Rakoto\",\"prenomsPrmp\":\"Nouvelle\",\"imPrmp\":\"IM7777\","
                + "\"arreteNomin\":\"ARR-2026-777\",\"dateNomin\":\"2026-01-01\",\"cin\":\"101010101010\","
                + "\"dateCin\":\"2010-01-01\",\"lieuCin\":\"Antananarivo\",\"emailPrmp\":\"new@prmp.mg\","
                + "\"telPrmp\":\"0340000000\"}";

        // Inscription publique (sans jeton) → 201, compte inactif.
        mvc.perform(post("/api/auth/register/prmp").contentType(MediaType.APPLICATION_JSON).content(inscription))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.actif").value(false))
                .andExpect(jsonPath("$.typeActeur").value("PRMP"));

        // L'Administrateur est notifié de l'inscription en attente.
        mvc.perform(get("/api/notifications").header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$[?(@.typeNotif=='NOUVELLE_INSCRIPTION')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.typeNotif=='NOUVELLE_INSCRIPTION')].destinataireIm", hasItem("CTRADM")));

        // Connexion refusée tant que le compte n'est pas validé → 401.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"prmp.new\",\"motDePasse\":\"Passw0rd!\"}"))
                .andExpect(status().isUnauthorized());

        // Un non-administrateur ne peut pas activer → 403.
        mvc.perform(post("/api/comptes-auth/prmp.new/activer").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());

        // L'Administrateur valide le compte.
        mvc.perform(post("/api/comptes-auth/prmp.new/activer").header("Authorization", tokenAdmin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.actif").value(true));

        // La connexion fonctionne désormais, avec le rôle PRMP.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"prmp.new\",\"motDePasse\":\"Passw0rd!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("PRMP"));

        // Réinscription avec le même login → 409.
        mvc.perform(post("/api/auth/register/prmp").contentType(MediaType.APPLICATION_JSON).content(inscription))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Login : le rôle et la localité sont déduits du profil")
    void login_resoutRoleEtLocalite() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"CTRCC1\",\"motDePasse\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CHEF_COMMISSION"))
                .andExpect(jsonPath("$.localite").value("ANT"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"CTRPRE\",\"motDePasse\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PRESIDENT"))
                .andExpect(jsonPath("$.localite").doesNotExist());

        // La PRMP n'a plus de localité propre : la claim localite est absente de sa réponse de connexion.
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"PRMP001\",\"motDePasse\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PRMP"))
                .andExpect(jsonPath("$.localite").doesNotExist());
    }

    @Test
    @DisplayName("Login : mauvais mot de passe → 401")
    void login_mauvaisMotDePasse() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"CTRCC1\",\"motDePasse\":\"faux\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoint protégé sans token → 401")
    void endpointProtege_sansToken() throws Exception {
        mvc.perform(get("/api/dossiers")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Référentiel public d'entités : accessible SANS jeton (écran d'inscription)")
    void entitesPubliques_sansToken() throws Exception {
        mvc.perform(get("/api/auth/entites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idEntiteContract==1)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idEntiteContract==1)].idLocalite", hasItem("ANT")));
    }

    @Test
    @DisplayName("Pièces jointes : stockage PDF (magic-bytes), remplacement par type, rejet d'un type non autorisé")
    void pieceJointe_stockageRemplacementRejet() throws Exception {
        byte[] pdf = "%PDF-1.4 contenu arrete".getBytes(StandardCharsets.US_ASCII);
        PieceJointeMetaDto meta = pieceJointeService.stocker("PRMP001", TypePieceJointe.ARRETE_NOMIN,
                new MockMultipartFile("arrete", "arrete.pdf", "application/pdf", pdf));
        assertTrue("application/pdf".equals(meta.format()), "format PDF détecté par magic-bytes");
        assertTrue(meta.hashSha256() != null && meta.hashSha256().length() == 64, "SHA-256 calculé");

        // Re-dépôt du même type → remplacement (le contenu récupéré est le plus récent).
        byte[] pdf2 = "%PDF-1.7 version corrigee".getBytes(StandardCharsets.US_ASCII);
        pieceJointeService.stocker("PRMP001", TypePieceJointe.ARRETE_NOMIN,
                new MockMultipartFile("arrete", "arrete2.pdf", "application/pdf", pdf2));
        byte[] recupere = pieceJointeService.telecharger("PRMP001", TypePieceJointe.ARRETE_NOMIN).getContenu();
        assertTrue(new String(recupere, StandardCharsets.US_ASCII).contains("version corrigee"),
                "le dernier dépôt remplace le précédent");

        // Type non autorisé (texte brut) → 400 (magic-bytes non reconnus).
        assertThrows(BadRequestException.class, () -> pieceJointeService.stocker("PRMP001",
                TypePieceJointe.CIN, new MockMultipartFile("cin", "cin.txt", "text/plain",
                        "ceci n'est pas une image".getBytes(StandardCharsets.US_ASCII))));
    }

    // ------------------------------------------------------------------
    // Autorisations par profil
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Référentiel : écriture interdite au Membre (403), permise à l'Admin (201)")
    void referentiel_ecritureAdminSeulement() throws Exception {
        String body = "{\"idLocalite\":\"TMS\",\"libelleLocalite\":\"Toamasina\","
                + "\"referencement\":\"REF-TMS\",\"localite\":\"TMS\"}";

        mvc.perform(post("/api/localites").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/localites").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Référentiel : lecture ouverte à tout utilisateur authentifié (200)")
    void referentiel_lectureOuverte() throws Exception {
        mvc.perform(get("/api/localites").header("Authorization", tokenMembre))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dispatch : interdit au Membre (403)")
    void dispatch_interditAuMembre() throws Exception {
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":9,\"idReception\":1,\"interimDispatch\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("INTERIM_DISPATCH : titulaire dans sa localité (false), intérim hors localité (true)")
    void interimDispatch_conditionnel() throws Exception {
        // Les gardes du dispatch exigent un dossier PRET_DISPATCH et une réception sans dispatch.
        // On prépare des dossiers PRET_DISPATCH avec une réception dédiée chacun (ANT et TMS).
        dossierRepository.save(dossier(10, "PRET_DISPATCH"));
        dossierRepository.save(dossier(11, "PRET_DISPATCH"));
        dossierRepository.save(dossier(12, "PRET_DISPATCH"));
        dossierRepository.save(dossier(13, "PRET_DISPATCH"));
        receptionRepository.save(reception(20, 10, "CTRSEC", true)); // ANT (CTRSEC = localité ANT)
        receptionRepository.save(reception(21, 11, "CTRSEC", true)); // ANT
        receptionRepository.save(reception(22, 12, "CTRCC2", true)); // TMS (CTRCC2 = localité TMS)
        receptionRepository.save(reception(23, 13, "CTRCC2", true)); // TMS

        // Cas conformes (CC d'ANT) — réceptions fraîches.
        // Dossier d'ANT en titulaire.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":30,\"idReception\":20,\"interimDispatch\":false}"))
                .andExpect(status().isCreated());
        // Dossier de TMS en intérim.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":31,\"idReception\":22,\"interimDispatch\":true}"))
                .andExpect(status().isCreated());

        // Cas non conformes (409) — la précondition passe (réceptions fraîches PRET_DISPATCH),
        // c'est la règle d'intérim qui bloque.
        // CC dans sa localité mais marqué intérim.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":32,\"idReception\":21,\"interimDispatch\":true}"))
                .andExpect(status().isConflict());
        // CC hors de sa localité mais marqué titulaire.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":33,\"idReception\":23,\"interimDispatch\":false}"))
                .andExpect(status().isConflict());
        // Le Président dispatche toujours en titulaire.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenPresident).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":34,\"idReception\":21,\"interimDispatch\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("KPIs : tableau de bord (pipeline + taux de conformité) réservé Président/Admin")
    void kpis_tableauBord() throws Exception {
        mvc.perform(get("/api/kpis/tableau-bord").header("Authorization", tokenPresident))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nbDossiersSoumis").value(2))
                .andExpect(jsonPath("$.nbDossiersConformes").value(0))
                .andExpect(jsonPath("$.tauxConformitePct").value(0.0))
                .andExpect(jsonPath("$.pipelineParStatut.EN_EXAMEN").value(2))
                .andExpect(jsonPath("$.topNonConformite").isArray());
        // Réservé : un Membre n'accède pas aux KPIs globaux.
        mvc.perform(get("/api/kpis/tableau-bord").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rapport PDF : généré pour le Président, interdit au Membre")
    void rapport_dossiersPdf() throws Exception {
        byte[] pdf = mvc.perform(get("/api/rapports/dossiers").header("Authorization", tokenPresident))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(pdf.length > 100, "le PDF doit être non vide");
        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).equals("%PDF"), "en-tête PDF attendu");

        // Un Membre ne peut pas générer le rapport (réservé Président / Admin).
        mvc.perform(get("/api/rapports/dossiers").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Export Excel : .xlsx généré pour le Président, interdit au Membre")
    void rapport_dossiersExcel() throws Exception {
        byte[] xlsx = mvc.perform(get("/api/rapports/dossiers/excel").header("Authorization", tokenPresident))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(xlsx.length > 100, "le classeur doit être non vide");
        assertTrue(xlsx[0] == 'P' && xlsx[1] == 'K', "signature ZIP/xlsx attendue (PK)");

        mvc.perform(get("/api/rapports/dossiers/excel").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rapport par localité : CC autorisé (forcé sur sa localité), Président peut cibler une localité")
    void rapport_parLocalite() throws Exception {
        // Le Chef de commission peut désormais générer le rapport : il est forcé sur sa propre localité (ANT).
        byte[] pdfCc = mvc.perform(get("/api/rapports/dossiers").header("Authorization", tokenCc))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(new String(pdfCc, 0, 4, StandardCharsets.US_ASCII).equals("%PDF"), "en-tête PDF attendu");

        // Idem en Excel.
        byte[] xlsxCc = mvc.perform(get("/api/rapports/dossiers/excel").header("Authorization", tokenCc))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(xlsxCc[0] == 'P' && xlsxCc[1] == 'K', "signature ZIP/xlsx attendue (PK)");

        // Le Président peut cibler explicitement une localité via ?localite=.
        mvc.perform(get("/api/rapports/dossiers").header("Authorization", tokenPresident).param("localite", "ANT"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("Messagerie : envoi, réception, marquage lu et confidentialité")
    void messagerie_envoiReceptionLu() throws Exception {
        // Le Membre envoie un message au CC.
        mvc.perform(post("/api/messages/envoyer").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"destinataireIm\":\"CTRCC1\",\"sujet\":\"Question\",\"corps\":\"Bonjour\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMessage").value(1))
                .andExpect(jsonPath("$.expediteurIm").value("CTRMEM"))
                .andExpect(jsonPath("$.destinataireIm").value("CTRCC1"))
                .andExpect(jsonPath("$.lu").value(false));

        // Boîte de réception du CC : 1 message ; envoyés du Membre : 1.
        mvc.perform(get("/api/messages/recus").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sujet").value("Question"));
        mvc.perform(get("/api/messages/envoyes").header("Authorization", tokenMembre))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        // Le CC marque le message comme lu.
        mvc.perform(post("/api/messages/1/lu").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lu").value(true));

        // L'expéditeur (non destinataire) ne peut pas marquer lu → 403.
        mvc.perform(post("/api/messages/1/lu").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());

        // Un tiers ne peut pas lire le message (confidentialité) → 403.
        mvc.perform(get("/api/messages/1").header("Authorization", tokenAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Audit automatique : une écriture API est tracée dans t_audit_log (§3.8)")
    void audit_traceLesEcritures() throws Exception {
        mvc.perform(post("/api/localites").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idLocalite\":\"TMS\",\"libelleLocalite\":\"Toamasina\","
                        + "\"referencement\":\"REF-TMS\",\"localite\":\"TMS\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/audit-logs").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomTable").value("localites"))
                .andExpect(jsonPath("$[0].typeAction").value("CREATE"))
                .andExpect(jsonPath("$[0].imActeur").value("CTRADM"));
    }

    @Test
    @DisplayName("Module 10 : écriture comptes/hiérarchie réservée Admin, lecture ouverte, sessions Admin-only")
    void module10_gestionComptes() throws Exception {
        // Création d'un contrôleur interdite au Membre (403), avant même la validation.
        mvc.perform(post("/api/controleurs").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imControleur\":\"CTRX\",\"idProfile\":5,\"transversal\":false}"))
                .andExpect(status().isForbidden());
        // Lecture des contrôleurs ouverte (l'UI affiche les noms).
        mvc.perform(get("/api/controleurs").header("Authorization", tokenMembre))
                .andExpect(status().isOk());
        // Sessions utilisateur : réservées à l'Administrateur (lecture comprise).
        mvc.perform(get("/api/session-utilisateurs").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/session-utilisateurs").header("Authorization", tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Visibilité dossiers : CC (localité ANT) et Président voient le dossier ANT")
    void visibilite_dossiers() throws Exception {
        // Le CC d'ANT ne voit que le dossier d'ANT (1), pas celui de TMS (2).
        mvc.perform(get("/api/dossiers").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        // Le Président voit toutes les localités (2 dossiers).
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPresident))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        // La PRMP voit ses propres dossiers (lien PPM → dossier).
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        // Une PRMP sans dossier ne voit rien.
        String tokenAutrePrmp = bearer("PRMPXX", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMPXX", "ANT");
        mvc.perform(get("/api/dossiers").header("Authorization", tokenAutrePrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Robustesse PK : création sans identifiant assigné → 400 (au lieu de 500)")
    void pk_idManquant_renvoie400() throws Exception {
        mvc.perform(post("/api/localites").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"libelleLocalite\":\"X\",\"referencement\":\"R\",\"localite\":\"X\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Délégation : tâche déléguée exécutable par le titulaire ou un profil délégué, sinon 403")
    void delegation_tachesDelegables() throws Exception {
        String body = "{\"idDossier\":1,\"numPassage\":1,\"typePassage\":\"INITIAL\","
                + "\"imCtrlRecept\":\"CTRCC1\",\"complet\":false}";
        // Secrétaire titulaire : autorisé.
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        mvc.perform(put("/api/receptions/1").header("Authorization", tokenSec)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        // CC délégué (délégation Secrétaire → CC active) : autorisé.
        mvc.perform(put("/api/receptions/1").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        // Membre sans délégation pour la réception : interdit.
        mvc.perform(put("/api/receptions/1").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Délégation limitée à la localité : agir sur un dossier d'une autre localité → 403")
    void delegation_contrainteLocalite() throws Exception {
        // Le Président (toutes localités) peut agir sur le dossier 2 (TMS).
        mvc.perform(post("/api/receptions").header("Authorization", tokenPresident)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":10,\"idDossier\":2,\"numPassage\":2,\"typePassage\":\"RETOUR\","
                        + "\"imCtrlRecept\":\"CTRPRE\",\"complet\":false}"))
                .andExpect(status().isCreated());
        // Le CC d'ANT, même délégué, ne peut pas agir sur un dossier de TMS → 403.
        mvc.perform(post("/api/receptions").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":9,\"idDossier\":2,\"numPassage\":2,\"typePassage\":\"RETOUR\","
                        + "\"imCtrlRecept\":\"CTRCC1\",\"complet\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Filtre localité étendu : réceptions/dispatch/examen limités à la localité")
    void filtreLocalite_etendu() throws Exception {
        // Réceptions : CC d'ANT n'en voit qu'une (ANT), le Président les deux.
        mvc.perform(get("/api/receptions").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/receptions").header("Authorization", tokenPresident))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        // Accès direct à une réception hors localité → 403 ; dans la localité → 200.
        mvc.perform(get("/api/receptions/2").header("Authorization", tokenCc))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/receptions/1").header("Authorization", tokenCc))
                .andExpect(status().isOk());
        // Dispatch et examen aussi filtrés (seuls ceux d'ANT existent).
        mvc.perform(get("/api/dispatchs").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/examens").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        // La PRMP n'accède pas aux ressources internes.
        mvc.perform(get("/api/receptions").header("Authorization", tokenPrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Scoping PPM/Marché : PRMP voit les siens, CC sa localité (hors brouillon), Président tout ; hors périmètre → 403")
    void scoping_ppmEtMarche() throws Exception {
        String tokenPrmp2 = bearer("PRMP002", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMP002", "ANT");
        prmpRepository.save(prmp("PRMP002", "ANT")); // FK t_dossier/t_ppm.ID_PRMP

        // Dossiers SOUMIS (estampillés localité) avec PPM/marché de PRMP différentes / localités différentes.
        dossierRepository.save(dossierLoc(200, "SOUMIS", "ANT", "PRMP001"));
        dossierRepository.save(dossierLoc(201, "SOUMIS", "ANT", "PRMP002"));
        dossierRepository.save(dossierLoc(202, "SOUMIS", "TMS", "PRMP001"));
        dossierRepository.save(dossierLoc(203, "BROUILLON", "ANT", "PRMP001")); // brouillon → invisible des contrôleurs
        ppmRepository.save(ppm(200, 200, "PRMP001"));
        ppmRepository.save(ppm(201, 201, "PRMP002"));
        ppmRepository.save(ppm(202, 202, "PRMP001"));
        ppmRepository.save(ppm(203, 203, "PRMP001"));
        marcheRepository.save(marche(800, 200, 200));
        marcheRepository.save(marche(801, 201, 201));
        marcheRepository.save(marche(802, 202, 202));

        // PRMP001 ne voit QUE ses PPM (200, 202, 203 — y compris son brouillon), pas ceux de PRMP002 (201).
        mvc.perform(get("/api/ppms").header("Authorization", tokenPrmp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idPpm==200)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idPpm==203)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idPpm==201)]", hasSize(0)));
        // PRMP002 ne voit que le sien (201).
        mvc.perform(get("/api/ppms").header("Authorization", tokenPrmp2))
                .andExpect(jsonPath("$[?(@.idPpm==201)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idPpm==200)]", hasSize(0)));
        // CC ANT voit les PPM ANT non brouillon (200, 201), pas TMS (202) ni le brouillon (203).
        mvc.perform(get("/api/ppms").header("Authorization", tokenCc))
                .andExpect(jsonPath("$[?(@.idPpm==200)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idPpm==201)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idPpm==202)]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.idPpm==203)]", hasSize(0)));
        // Président voit tout, y compris TMS (202).
        mvc.perform(get("/api/ppms").header("Authorization", tokenPresident))
                .andExpect(jsonPath("$[?(@.idPpm==202)]", hasSize(1)));

        // GET /{id} hors périmètre → 403 : PRMP001 sur le PPM de PRMP002 ; CC sur un brouillon.
        mvc.perform(get("/api/ppms/201").header("Authorization", tokenPrmp))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/ppms/203").header("Authorization", tokenCc))
                .andExpect(status().isForbidden());

        // Marchés : même scoping. PRMP001 voit 800/802 mais pas 801 ; CC ANT voit 800 mais pas 802 (TMS).
        mvc.perform(get("/api/marches").header("Authorization", tokenPrmp))
                .andExpect(jsonPath("$[?(@.idDetail==800)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDetail==801)]", hasSize(0)));
        mvc.perform(get("/api/marches").header("Authorization", tokenCc))
                .andExpect(jsonPath("$[?(@.idDetail==800)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDetail==802)]", hasSize(0)));
        // GET /{id} : marché de PRMP002 → 403 pour PRMP001 ; marché ANT visible au Membre d'ANT.
        mvc.perform(get("/api/marches/801").header("Authorization", tokenPrmp))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/marches/800").header("Authorization", tokenMembre))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Filtre serveur ?statut sur /api/dossiers : scoping conservé, statut inconnu → 400")
    void dossiers_filtreStatut() throws Exception {
        dossierRepository.save(dossierLoc(210, "SOUMIS", "ANT", "PRMP001"));
        dossierRepository.save(dossierLoc(211, "BROUILLON", "ANT", "PRMP001"));
        dossierRepository.save(dossierLoc(212, "CLOTURE", "ANT", "PRMP001"));

        // PRMP001 : ?statut=SOUMIS ne renvoie que 210 (pas 211 brouillon ni 212 clôturé).
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPrmp).param("statut", "SOUMIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idDossier==210)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==211)]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.idDossier==212)]", hasSize(0)));
        // ?statut=BROUILLON renvoie 211 (la PRMP voit ses brouillons).
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPrmp).param("statut", "BROUILLON"))
                .andExpect(jsonPath("$[?(@.idDossier==211)]", hasSize(1)));
        // Sans filtre : 210, 211 et 212 présents.
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPrmp))
                .andExpect(jsonPath("$[?(@.idDossier==210)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==212)]", hasSize(1)));
        // Scoping conservé : le CC ANT avec ?statut=SOUMIS voit 210, jamais le brouillon 211.
        mvc.perform(get("/api/dossiers").header("Authorization", tokenCc).param("statut", "SOUMIS"))
                .andExpect(jsonPath("$[?(@.idDossier==210)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==211)]", hasSize(0)));
        // Statut inconnu → 400.
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPrmp).param("statut", "NIMPORTEQUOI"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Réceptions : filtre ?idDossier scopé et test /existe (déjà réceptionné) sans charger l'historique")
    void receptions_parDossierEtExiste() throws Exception {
        // Dossier ANT déjà réceptionné = dossier 1 (réception 1, CTRCC1). Dossier ANT sans réception = 220.
        dossierRepository.save(dossierLoc(220, "SOUMIS", "ANT", "PRMP001"));

        // CC ANT : ?idDossier=1 ne renvoie que la réception du dossier 1.
        mvc.perform(get("/api/receptions").header("Authorization", tokenCc).param("idDossier", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        // /existe : dossier 1 → reçu ; dossier 220 (aucune réception) → non reçu.
        mvc.perform(get("/api/receptions/dossier/1/existe").header("Authorization", tokenCc))
                .andExpect(jsonPath("$.recu").value(true));
        mvc.perform(get("/api/receptions/dossier/220/existe").header("Authorization", tokenCc))
                .andExpect(jsonPath("$.recu").value(false));
        // Isolation localité : CC ANT n'obtient pas les réceptions du dossier 2 (TMS).
        mvc.perform(get("/api/receptions").header("Authorization", tokenCc).param("idDossier", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        // La PRMP (ressource interne) → liste vide même par dossier.
        mvc.perform(get("/api/receptions").header("Authorization", tokenPrmp).param("idDossier", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Filtre demandes de retrait : PRMP voit les siennes, CC celles de sa localité")
    void filtreLocalite_demandeRetrait() throws Exception {
        // PRMP001 voit sa demande.
        mvc.perform(get("/api/demande-retraits").header("Authorization", tokenPrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        // Le CC d'ANT voit la demande de sa localité (dossier ANT).
        mvc.perform(get("/api/demande-retraits").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        // Une autre PRMP ne voit rien.
        String tokenAutrePrmp = bearer("PRMPXX", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMPXX", "ANT");
        mvc.perform(get("/api/demande-retraits").header("Authorization", tokenAutrePrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    // ------------------------------------------------------------------
    // Workflow du PV
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Workflow PV : cycle complet BROUILLON → SIGNE avec gardes et navette")
    void workflowPv_cycleComplet() throws Exception {
        // Création : le statut envoyé (SIGNE) est ignoré, le PV démarre en BROUILLON.
        mvc.perform(post("/api/pv-examens").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPv\":1,\"idExamen\":1,\"idAvis\":\"FAV\",\"imCtrlMembre\":\"CTRMEM\","
                        + "\"statutPv\":\"SIGNE\",\"nbNavettes\":99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statutPv").value("BROUILLON"))
                .andExpect(jsonPath("$.nbNavettes").value(0));

        soumettre(tokenMembre).andExpect(status().isOk())
                .andExpect(jsonPath("$.statutPv").value("PROJET_SOUMIS"));

        // Retour interdit au Membre.
        mvc.perform(post("/api/pv-examens/1/retourner").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRMEM\",\"commentaire\":\"x\"}"))
                .andExpect(status().isForbidden());

        // Retour sans commentaire interdit (garde métier).
        mvc.perform(post("/api/pv-examens/1/retourner").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRCC1\"}"))
                .andExpect(status().isConflict());

        // Retour valide par le CC.
        mvc.perform(post("/api/pv-examens/1/retourner").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imActeur\":\"CTRCC1\",\"commentaire\":\"Corriger la synthèse\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statutPv").value("EN_RECTIFICATION"));

        soumettre(tokenMembre).andExpect(status().isOk())
                .andExpect(jsonPath("$.statutPv").value("PROJET_SOUMIS"));

        mvc.perform(post("/api/pv-examens/1/accepter").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRCC1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statutPv").value("PROJET_ACCEPTE"));

        // Une seule signature ne suffit pas.
        signer(tokenMembre, "CTRMEM", "MEMBRE").andExpect(status().isOk())
                .andExpect(jsonPath("$.statutPv").value("PROJET_ACCEPTE"));

        // Co-signature → SIGNE.
        signer(tokenPresident, "CTRPRE", "PRESIDENT").andExpect(status().isOk())
                .andExpect(jsonPath("$.statutPv").value("SIGNE"))
                .andExpect(jsonPath("$.datePv").isNotEmpty());

        // 4 navettes tracées (SOUMISSION, RETOUR_RECTIF, SOUMISSION, ACCEPTATION).
        mvc.perform(get("/api/pv-navettes").header("Authorization", tokenMembre))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));

        // PV signé non éditable.
        mvc.perform(put("/api/pv-examens/1").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPv\":1,\"idExamen\":1,\"idAvis\":\"FAV\",\"imCtrlMembre\":\"CTRMEM\","
                        + "\"statutPv\":\"SIGNE\",\"nbNavettes\":4}"))
                .andExpect(status().isConflict());

        // Navette non supprimable.
        mvc.perform(delete("/api/pv-navettes/1").header("Authorization", tokenMembre))
                .andExpect(status().isConflict());

        // [Auto] La PRMP du dossier reçoit une notification PV_SIGNE.
        mvc.perform(get("/api/notifications").header("Authorization", tokenCc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.typeNotif=='PV_SIGNE')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.typeNotif=='PV_SIGNE')].destinataireEmail", hasItem("prmp@min.mg")));
    }

    // ------------------------------------------------------------------
    // Comportements automatiques
    // ------------------------------------------------------------------

    @Test
    @DisplayName("[Auto] Réception complète → dossier PRET_DISPATCH")
    void auto_pretDispatch() throws Exception {
        mvc.perform(put("/api/receptions/1").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":1,\"numPassage\":1,\"typePassage\":\"INITIAL\","
                        + "\"imCtrlRecept\":\"CTRCC1\",\"complet\":true}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/dossiers/1").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("PRET_DISPATCH"));

        // [Auto] Notification PRET_DISPATCH adressée au Président et au CC de la localité.
        mvc.perform(get("/api/notifications").header("Authorization", tokenCc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.typeNotif=='PRET_DISPATCH')]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.typeNotif=='PRET_DISPATCH')].destinataireIm", hasItem("CTRPRE")))
                .andExpect(jsonPath("$[?(@.typeNotif=='PRET_DISPATCH')].destinataireIm", hasItem("CTRCC1")));
    }

    @Test
    @DisplayName("[Auto] Vérification obs. levées → dossier CLOTURE")
    void auto_cloture() throws Exception {
        // La vérification exige un PV au statut SIGNE (§3.6) : on amène le PV jusqu'à SIGNE.
        mvc.perform(post("/api/pv-examens").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPv\":1,\"idExamen\":1,\"idAvis\":\"FAV\",\"imCtrlMembre\":\"CTRMEM\","
                        + "\"statutPv\":\"BROUILLON\",\"nbNavettes\":0}"))
                .andExpect(status().isCreated());
        soumettre(tokenMembre).andExpect(status().isOk());
        mvc.perform(post("/api/pv-examens/1/accepter").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRCC1\"}"))
                .andExpect(status().isOk());
        signer(tokenMembre, "CTRMEM", "MEMBRE").andExpect(status().isOk());
        signer(tokenPresident, "CTRPRE", "PRESIDENT").andExpect(status().isOk())
                .andExpect(jsonPath("$.statutPv").value("SIGNE"));

        mvc.perform(post("/api/verifications").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idVerification\":1,\"idReception\":1,\"idPv\":1,\"imCtrlVerif\":\"CTRCC1\","
                        + "\"obsLevees\":true}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/dossiers/1").header("Authorization", tokenCc))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("CLOTURE"));

        // [Auto] Le Chargé de publication est alerté que le dossier clôturé est éligible.
        mvc.perform(get("/api/notifications").header("Authorization", tokenCc))
                .andExpect(jsonPath("$[?(@.typeNotif=='CLOTURE_ELIGIBLE')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.typeNotif=='CLOTURE_ELIGIBLE')].destinataireIm", hasItem("CTRPUB")));
    }

    @Test
    @DisplayName("Circuit complet : Réception → PRET_DISPATCH → Dispatch → Examen → PV(navette → SIGNE) → Vérification → CLOTURE")
    void circuitComplet_boutEnBout() throws Exception {
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        // Dossier de test neuf (id 3), distinct des dossiers seedés.
        dossierRepository.save(dossier(3, "EN_EXAMEN"));

        // 1) Réception complète par le Secrétaire → [Auto] dossier PRET_DISPATCH.
        mvc.perform(post("/api/receptions").header("Authorization", tokenSec)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":3,\"idDossier\":3,\"numPassage\":1,\"typePassage\":\"INITIAL\","
                        + "\"imCtrlRecept\":\"CTRSEC\",\"complet\":true}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/dossiers/3").header("Authorization", tokenPresident))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("PRET_DISPATCH"));

        // 2) Dispatch par le CC (titulaire dans sa localité ANT).
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":3,\"idReception\":3,\"imCtrlDispatch\":\"CTRCC1\",\"imCtrlCc\":\"CTRCC1\","
                        + "\"imCtrlMembre\":\"CTRMEM\",\"interimDispatch\":false}"))
                .andExpect(status().isCreated());
        // Le dispatch fait avancer le dossier à DISPATCHE (règle ajoutée).
        mvc.perform(get("/api/dossiers/3").header("Authorization", tokenPresident))
                .andExpect(jsonPath("$.statut").value("DISPATCHE"));

        // 3) Examen par le Membre.
        mvc.perform(post("/api/examens").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idExamen\":3,\"idDispatch\":3,\"imCtrlMembre\":\"CTRMEM\"}"))
                .andExpect(status().isCreated());

        // 4) Projet de PV par le Membre → toujours créé en BROUILLON.
        mvc.perform(post("/api/pv-examens").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPv\":3,\"idExamen\":3,\"idAvis\":\"FAV\",\"imCtrlMembre\":\"CTRMEM\","
                        + "\"statutPv\":\"BROUILLON\",\"nbNavettes\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statutPv").value("BROUILLON"));

        // 5) Navette : soumettre → accepter, puis co-signature Membre + Président → SIGNE.
        mvc.perform(post("/api/pv-examens/3/soumettre").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRMEM\"}"))
                .andExpect(jsonPath("$.statutPv").value("PROJET_SOUMIS"));
        mvc.perform(post("/api/pv-examens/3/accepter").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRCC1\"}"))
                .andExpect(jsonPath("$.statutPv").value("PROJET_ACCEPTE"));
        // Un seul signataire ne suffit pas : le PV reste PROJET_ACCEPTE.
        mvc.perform(post("/api/pv-examens/3/signer").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRMEM\",\"role\":\"MEMBRE\"}"))
                .andExpect(jsonPath("$.statutPv").value("PROJET_ACCEPTE"));
        mvc.perform(post("/api/pv-examens/3/signer").header("Authorization", tokenPresident)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRPRE\",\"role\":\"PRESIDENT\"}"))
                .andExpect(jsonPath("$.statutPv").value("SIGNE"));

        // 6) Vérification avec observations levées → [Auto] dossier CLOTURE.
        mvc.perform(post("/api/verifications").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idVerification\":3,\"idReception\":3,\"idPv\":3,\"imCtrlVerif\":\"CTRCC1\","
                        + "\"obsLevees\":true}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/dossiers/3").header("Authorization", tokenPresident))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("CLOTURE"));
    }

    @Test
    @DisplayName("Transitions interdites : rôle non autorisé → 403, saut d'étape du PV → 409")
    void transitionsInterdites() throws Exception {
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        String tokenVer = bearer("CTRVER", ProfilUtilisateur.VERIFICATEUR, TypeActeur.CONTROLEUR, "CTRVER", "ANT");

        // Rôle : un Vérificateur ne peut pas dispatcher → 403.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenVer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":1,\"interimDispatch\":false}"))
                .andExpect(status().isForbidden());

        // Rôle : un Secrétaire ne peut pas accepter un projet de PV (réservé CC / Président) → 403.
        mvc.perform(post("/api/pv-examens/1/accepter").header("Authorization", tokenSec)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRSEC\"}"))
                .andExpect(status().isForbidden());

        // Saut d'étape : un PV en BROUILLON ne peut être ni accepté ni signé → 409.
        mvc.perform(post("/api/pv-examens").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPv\":4,\"idExamen\":1,\"idAvis\":\"FAV\",\"imCtrlMembre\":\"CTRMEM\","
                        + "\"statutPv\":\"BROUILLON\",\"nbNavettes\":0}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/pv-examens/4/accepter").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRCC1\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/pv-examens/4/signer").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRMEM\",\"role\":\"MEMBRE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Préconditions du circuit : dispatch hors PRET_DISPATCH / doublon, examen hors circuit, vérif hors PV SIGNE → 409")
    void preconditionsCircuit_bloquent() throws Exception {
        // (a) Dispatch d'un dossier non PRET_DISPATCH (dossier 2 = EN_EXAMEN, réception 2 sans dispatch) → 409.
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenPresident).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":40,\"idReception\":2,\"interimDispatch\":false}"))
                .andExpect(status().isConflict());

        // (b) Anti-doublon : un dossier PRET_DISPATCH qui a déjà un dispatch → 2e dispatch refusé.
        dossierRepository.save(dossier(14, "PRET_DISPATCH"));
        receptionRepository.save(reception(24, 14, "CTRSEC", true));
        dispatchRepository.save(dispatch(41, 24, "CTRCC1", "CTRMEM"));
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":42,\"idReception\":24,\"interimDispatch\":false}"))
                .andExpect(status().isConflict());

        // (c) Examen d'un dossier non dispatché (dispatch 1 → dossier 1 = EN_EXAMEN, pas DISPATCHE) → 409.
        mvc.perform(post("/api/examens").header("Authorization", tokenMembre).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idExamen\":40,\"idDispatch\":1,\"imCtrlMembre\":\"CTRMEM\"}"))
                .andExpect(status().isConflict());

        // (d) Vérification sur un PV non SIGNE (BROUILLON) → 409.
        mvc.perform(post("/api/pv-examens").header("Authorization", tokenMembre).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPv\":5,\"idExamen\":1,\"idAvis\":\"FAV\",\"imCtrlMembre\":\"CTRMEM\","
                        + "\"statutPv\":\"BROUILLON\",\"nbNavettes\":0}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/verifications").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idVerification\":40,\"idReception\":1,\"idPv\":5,\"imCtrlVerif\":\"CTRCC1\","
                        + "\"obsLevees\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Dispatch → dossier DISPATCHE ; examen refusé tant que le dossier n'est pas dispatché")
    void dispatch_avanceDossierADispatche() throws Exception {
        // A) Dossier PRET_DISPATCH avec un dispatch SEEDÉ en direct (le dossier reste PRET_DISPATCH) :
        //    l'examen est refusé car le dossier n'est pas DISPATCHE.
        dossierRepository.save(dossier(15, "PRET_DISPATCH"));
        receptionRepository.save(reception(25, 15, "CTRSEC", true));
        dispatchRepository.save(dispatch(45, 25, "CTRCC1", "CTRMEM"));
        mvc.perform(post("/api/examens").header("Authorization", tokenMembre).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idExamen\":45,\"idDispatch\":45,\"imCtrlMembre\":\"CTRMEM\"}"))
                .andExpect(status().isConflict());

        // B) Dispatch VIA L'API → le dossier passe à DISPATCHE, et l'examen devient alors permis.
        dossierRepository.save(dossier(16, "PRET_DISPATCH"));
        receptionRepository.save(reception(26, 16, "CTRSEC", true));
        mvc.perform(post("/api/dispatchs").header("Authorization", tokenCc).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispatch\":46,\"idReception\":26,\"interimDispatch\":false}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/dossiers/16").header("Authorization", tokenPresident))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("DISPATCHE"));
        mvc.perform(post("/api/examens").header("Authorization", tokenMembre).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idExamen\":46,\"idDispatch\":46,\"imCtrlMembre\":\"CTRMEM\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Soumission dossier (§3.1, Option C) : la PRMP soumet → référence générée + Secrétaire/CC notifiés ; re-soumission → 409")
    void soumissionDossier_ok() throws Exception {
        // Brouillon PPM de la PRMP courante (PRMP001), localisé ANT, avec son PPM.
        Dossier d = dossier(3, "BROUILLON");
        d.setRefeDossier(null);
        d.setIdTypeDossier("PPM");
        d.setIdPrmp("PRMP001");
        d.setIdLocalite("ANT");
        dossierRepository.save(d);
        Ppm ppm = ppmLocalise(30, 3, "ANT");
        ppm.setIdPrmp("PRMP001");
        ppmRepository.save(ppm);
        marcheRepository.save(marche(31, 3, 30)); // un PPM doit comporter au moins un marché (règle ajoutée)

        // Soumission par la PRMP → 200, statut SOUMIS, référence unique générée.
        mvc.perform(post("/api/dossiers/3/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SOUMIS"))
                .andExpect(jsonPath("$.refeDossier", startsWith("CNM-ANT-2026-")));

        // Le Secrétaire et le CC de la localité sont notifiés.
        mvc.perform(get("/api/notifications").header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$[?(@.typeNotif=='DOSSIER_SOUMIS')].destinataireIm", hasItem("CTRSEC")))
                .andExpect(jsonPath("$[?(@.typeNotif=='DOSSIER_SOUMIS')].destinataireIm", hasItem("CTRCC1")));

        // Re-soumission → 409 (le dossier n'est plus BROUILLON).
        mvc.perform(post("/api/dossiers/3/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Soumission dossier (§3.1) refus : dossier d'une autre PRMP → 403, localité indéterminable → 400, non-PRMP → 403")
    void soumissionDossier_refus() throws Exception {
        // Jeton PRMP SANS localité (pour forcer l'échec de résolution de localité).
        String tokenPrmpSansLoc = bearer("PRMP001", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMP001", null);
        // Une autre PRMP propriétaire.
        prmpRepository.save(prmp("PRMPXX", "ANT"));
        // (4) Brouillon DAO appartenant à une AUTRE PRMP.
        Dossier d4 = dossier(4, "BROUILLON");
        d4.setIdTypeDossier("DAO");
        d4.setIdPrmp("PRMPXX");
        dossierRepository.save(d4);
        // (5) Brouillon DAO de PRMP001, sans localité ni PPM.
        Dossier d5 = dossier(5, "BROUILLON");
        d5.setRefeDossier(null);
        d5.setIdTypeDossier("DAO");
        d5.setIdPrmp("PRMP001");
        dossierRepository.save(d5);

        // (a) Dossier d'une autre PRMP → 403.
        mvc.perform(post("/api/dossiers/4/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isForbidden());
        // (b) Dossier nu + PRMP sans localité → aucune localité résoluble → 400.
        mvc.perform(post("/api/dossiers/5/soumettre").header("Authorization", tokenPrmpSansLoc))
                .andExpect(status().isBadRequest());
        // (c) Un non-PRMP ne peut pas soumettre → 403.
        mvc.perform(post("/api/dossiers/5/soumettre").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Soumission dossier SANS PPM (DAO/MAOO) : la localité du dossier (dérivée de l'entité à la saisie) → référence + ID_LOCALITE estampillé + Secrétaire notifié et le voit")
    void soumissionDossier_sansPpm() throws Exception {
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        // Brouillon DAO sans PPM, de PRMP001, dont la localité (ANT) a été dérivée de l'entité à la saisie.
        Dossier d = dossier(6, "BROUILLON");
        d.setRefeDossier(null);
        d.setIdTypeDossier("DAO");
        d.setIdPrmp("PRMP001");
        d.setIdLocalite("ANT");
        dossierRepository.save(d);

        // PRMP001 soumet → localité = ANT (celle du dossier), SOUMIS, référence + ID_LOCALITE (plus de repli PRMP).
        mvc.perform(post("/api/dossiers/6/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SOUMIS"))
                .andExpect(jsonPath("$.refeDossier", startsWith("CNM-ANT-")))
                .andExpect(jsonPath("$.idLocalite").value("ANT"));
        // Le Secrétaire de la localité (ANT) est notifié.
        mvc.perform(get("/api/notifications").header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$[?(@.typeNotif=='DOSSIER_SOUMIS')].destinataireIm", hasItem("CTRSEC")));
        // Et le dossier est désormais visible par le Secrétaire AVANT toute réception (via ID_LOCALITE).
        mvc.perform(get("/api/dossiers/6").header("Authorization", tokenSec))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Affectations PRMP↔entité (§3.1) : lecture scopée, unicité une PRMP active par entité (409), écriture Admin only")
    void prmpEntites_scopeUniciteEtAutorisation() throws Exception {
        // Lecture scopée : l'Administrateur voit toutes les affectations (les 2 seedées de PRMP001).
        mvc.perform(get("/api/prmp-entites").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idPrmp=='PRMP001')]", hasSize(2)));
        // La PRMP ne voit que les siennes.
        mvc.perform(get("/api/prmp-entites").header("Authorization", tokenPrmp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.idPrmp=='PRMP001')]", hasSize(2)));
        // Une autre PRMP (sans affectation) ne voit rien.
        String tokenPrmp2 = bearer("PRMP002", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMP002", null);
        mvc.perform(get("/api/prmp-entites").header("Authorization", tokenPrmp2))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        // Un contrôleur (ni Admin ni PRMP) → liste vide.
        mvc.perform(get("/api/prmp-entites").header("Authorization", tokenMembre))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        // Unicité : l'entité 1 est déjà rattachée à PRMP001 → tentative pour une autre PRMP → 409.
        prmpRepository.save(prmp("PRMP002", "ANT"));
        mvc.perform(post("/api/prmp-entites").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPrmp\":\"PRMP002\",\"idEntiteContract\":1,\"actif\":true}"))
                .andExpect(status().isConflict());

        // Écriture réservée à l'Admin : une PRMP ne peut pas créer d'affectation → 403.
        entiteContractRepository.save(entite(3, 1, "ANT"));
        mvc.perform(post("/api/prmp-entites").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPrmp\":\"PRMP001\",\"idEntiteContract\":3,\"actif\":true}"))
                .andExpect(status().isForbidden());

        // L'Admin affecte une entité libre (3) à PRMP001 → 201, active.
        mvc.perform(post("/api/prmp-entites").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPrmp\":\"PRMP001\",\"idEntiteContract\":3,\"actif\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEntiteContract").value(3))
                .andExpect(jsonPath("$.actif").value(true));
    }

    @Test
    @DisplayName("Visibilité dossier via ID_LOCALITE : dossier localisé (sans PPM ni réception) visible par sa localité, pas une autre")
    void visibiliteDossierViaIdLocalite() throws Exception {
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        // Dossiers estampillés, sans PPM ni réception.
        Dossier dAnt = dossier(7, "RECU");
        dAnt.setIdLocalite("ANT");
        dossierRepository.save(dAnt);
        Dossier dTms = dossier(8, "RECU");
        dTms.setIdLocalite("TMS");
        dossierRepository.save(dTms);

        mvc.perform(get("/api/dossiers").header("Authorization", tokenSec))
                .andExpect(jsonPath("$[?(@.idDossier==7)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==8)]", hasSize(0)));
        mvc.perform(get("/api/dossiers/7").header("Authorization", tokenSec)).andExpect(status().isOk());
        mvc.perform(get("/api/dossiers/8").header("Authorization", tokenSec)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Visibilité dossier via PPM (Option A) : un dossier à PPM de la localité est visible/consultable sans réception")
    void visibiliteDossierViaPpm() throws Exception {
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        // Dossier soumis (PPM de localité ANT), AUCUNE réception.
        dossierRepository.save(dossier(3, "RECU"));
        ppmRepository.save(ppmLocalise(30, 3, "ANT"));
        // Dossier soumis (PPM de localité TMS), AUCUNE réception.
        dossierRepository.save(dossier(4, "RECU"));
        ppmRepository.save(ppmLocalise(40, 4, "TMS"));

        // Le Secrétaire d'ANT voit le dossier 3 (PPM de sa localité), pas le 4 (PPM TMS).
        mvc.perform(get("/api/dossiers").header("Authorization", tokenSec))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idDossier==3)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==4)]", hasSize(0)));

        // Accès direct : 200 sur le 3 (sa localité via PPM), 403 sur le 4 (autre localité).
        mvc.perform(get("/api/dossiers/3").header("Authorization", tokenSec))
                .andExpect(status().isOk());
        mvc.perform(get("/api/dossiers/4").header("Authorization", tokenSec))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Garde réception : la 1ʳᵉ réception doit se faire dans la localité du dossier (via ID_LOCALITE)")
    void receptionDansLocaliteDuDossier() throws Exception {
        // Dossier estampillé TMS, aucune réception préalable.
        Dossier d = dossier(9, "RECU");
        d.setIdLocalite("TMS");
        dossierRepository.save(d);

        // Le Président (toutes localités) peut réceptionner (succès d'abord → pas de rollback-only).
        mvc.perform(post("/api/receptions").header("Authorization", tokenPresident)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":91,\"idDossier\":9,\"numPassage\":1,\"typePassage\":\"INITIAL\","
                        + "\"imCtrlRecept\":\"CTRPRE\",\"complet\":false}"))
                .andExpect(status().isCreated());
        // Un contrôleur d'ANT (CC, délégué Secrétaire) ne peut pas réceptionner un dossier TMS → 403.
        mvc.perform(post("/api/receptions").header("Authorization", tokenCc)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":92,\"idDossier\":9,\"numPassage\":1,\"typePassage\":\"INITIAL\","
                        + "\"imCtrlRecept\":\"CTRCC1\",\"complet\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Façade saisie PPM : dossier BROUILLON + PPM + marché (mode auto), invisible des contrôleurs puis visible après soumission")
    void saisiePpm_facade() throws Exception {
        natureRepository.save(new Nature(1, "Travaux", null));
        situationRepository.save(new Situation(1, "Normale", null));
        modePassationRepository.save(new ModePassation(2, "AOR", null, null, null, null));
        seuilRepository.save(seuil(902, "ANT", 1, "200000001", "1000000000"));
        reglePassationRepository.save(regle(902, 1, 902, 2));
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");

        // Localité dérivée de l'entité 1 (= ANT) ; pas de idLocalite dans le corps.
        String body = "{\"idDossier\":60,\"idEntiteContract\":1,\"idPpm\":60,\"exercice\":2026,"
                + "\"signataire\":\"RABE\",\"dateSignature\":\"2026-01-10\",\"reference\":\"PPM-60\","
                + "\"marches\":[{\"idDetail\":600,\"montEstim\":500000000,\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}]}";
        mvc.perform(post("/api/saisies/ppm").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andExpect(jsonPath("$.idTypeDossier").value("PPM"))
                .andExpect(jsonPath("$.idLocalite").value("ANT"))
                .andExpect(jsonPath("$.idPrmp").value("PRMP001"));
        // La ligne de marché a son mode déterminé automatiquement (AOR = 2).
        mvc.perform(get("/api/marches/600").header("Authorization", tokenPrmp))
                .andExpect(jsonPath("$.idMode").value(2));
        // Le brouillon est invisible du Secrétaire.
        mvc.perform(get("/api/dossiers/60").header("Authorization", tokenSec))
                .andExpect(status().isForbidden());
        // Soumission → SOUMIS → devient visible.
        mvc.perform(post("/api/dossiers/60/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("SOUMIS"));
        mvc.perform(get("/api/dossiers/60").header("Authorization", tokenSec))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Façade saisie DAO : dossier DAO BROUILLON ; type PPM refusé")
    void saisieDossier_dao() throws Exception {
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":61,\"idTypeDossier\":\"DAO\",\"idEntiteContract\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andExpect(jsonPath("$.idTypeDossier").value("DAO"))
                .andExpect(jsonPath("$.idLocalite").value("ANT"))      // dérivée de l'entité 1
                .andExpect(jsonPath("$.idEntiteContract").value(1));
        // Le type PPM est refusé par cette façade (utiliser /api/saisies/ppm).
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":62,\"idTypeDossier\":\"PPM\",\"idEntiteContract\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Intégrité type↔contenu : PPM sans t_ppm → soumission 409 ; PPM attaché à un dossier DAO → 409")
    void integrite_typeContenu() throws Exception {
        // Brouillon PPM sans aucun t_ppm rattaché → soumission refusée.
        Dossier dPpmVide = dossier(63, "BROUILLON");
        dPpmVide.setIdTypeDossier("PPM");
        dPpmVide.setIdPrmp("PRMP001");
        dPpmVide.setIdLocalite("ANT");
        dossierRepository.save(dPpmVide);
        mvc.perform(post("/api/dossiers/63/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isConflict());

        // Brouillon DAO (sans propriétaire) ; y attacher un PPM via l'endpoint Admin → 409.
        Dossier dDao = dossier(64, "BROUILLON");
        dDao.setIdTypeDossier("DAO");
        dossierRepository.save(dDao);
        mvc.perform(post("/api/ppms").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPpm\":64,\"idDossier\":64,\"exercice\":2026,\"signataire\":\"X\","
                        + "\"dateSignature\":\"2026-01-10\",\"reference\":\"P64\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Soumission PPM : un PPM sans marché → 409 ; avec au moins un marché → OK (⚠️ règle ajoutée)")
    void soumission_ppmSansMarche() throws Exception {
        // Brouillon PPM de PRMP001 avec son t_ppm mais AUCUN marché → soumission refusée (409).
        Dossier d = dossier(90, "BROUILLON");
        d.setIdTypeDossier("PPM");
        d.setIdPrmp("PRMP001");
        d.setIdLocalite("ANT");
        dossierRepository.save(d);
        ppmRepository.save(ppm(90, 90, "PRMP001"));
        mvc.perform(post("/api/dossiers/90/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isConflict());

        // Ajout d'au moins une ligne de marché → la soumission passe (SOUMIS).
        marcheRepository.save(marche(900, 90, 90));
        mvc.perform(post("/api/dossiers/90/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SOUMIS"));
    }

    @Test
    @DisplayName("Endpoints bruts restreints : POST /api/dossiers et /api/ppms réservés Admin ; façade réservée PRMP")
    void endpointsBruts_restreints() throws Exception {
        String dossierBody = "{\"idDossier\":65,\"statut\":\"BROUILLON\"}";
        // PRMP ne peut pas créer un dossier brut → 403 ; Admin → 201.
        mvc.perform(post("/api/dossiers").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON).content(dossierBody))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/dossiers").header("Authorization", tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(dossierBody))
                .andExpect(status().isCreated());
        // PRMP ne peut pas créer un PPM brut → 403.
        mvc.perform(post("/api/ppms").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPpm\":65,\"idDossier\":65,\"exercice\":2026,\"signataire\":\"X\","
                        + "\"dateSignature\":\"2026-01-10\",\"reference\":\"P65\"}"))
                .andExpect(status().isForbidden());
        // La façade de saisie est réservée PRMP : un Membre → 403.
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenMembre)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":66,\"idTypeDossier\":\"DAO\",\"idEntiteContract\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Reprise brouillon PRMP : la PRMP voit/rouvre son brouillon DAO (via t_dossier.idPrmp), pas une autre PRMP")
    void brouillonDao_visiblePourSaProprePrmp() throws Exception {
        String tokenAutrePrmp = bearer("PRMPYY", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMPYY", "ANT");
        // Brouillon DAO de PRMP001 (aucun PPM).
        Dossier d = dossier(80, "BROUILLON");
        d.setIdTypeDossier("DAO");
        d.setIdPrmp("PRMP001");
        dossierRepository.save(d);

        // PRMP001 voit son brouillon dans sa liste et peut l'ouvrir.
        mvc.perform(get("/api/dossiers").header("Authorization", tokenPrmp))
                .andExpect(jsonPath("$[?(@.idDossier==80)]", hasSize(1)));
        mvc.perform(get("/api/dossiers/80").header("Authorization", tokenPrmp))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value("BROUILLON"));
        // Une autre PRMP ne le voit pas.
        mvc.perform(get("/api/dossiers").header("Authorization", tokenAutrePrmp))
                .andExpect(jsonPath("$[?(@.idDossier==80)]", hasSize(0)));
        mvc.perform(get("/api/dossiers/80").header("Authorization", tokenAutrePrmp))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Saisie : la localité vient de l'ENTITÉ choisie (même PRMP, 2 localités) ; entité hors PRMP → 403 ; entité sans localité → 400")
    void saisieLocalite_deLEntite() throws Exception {
        // Entité 9 existe mais non affectée à PRMP001 ; entité 10 affectée mais sans localité.
        entiteContractRepository.save(entite(9, 1, "ANT"));
        entiteContractRepository.save(entite(10, 1, null));
        prmpEntiteRepository.save(prmpEntite(10, "PRMP001", 10, true));

        // Même PRMP (PRMP001), 2 entités de localités différentes → 2 dossiers de localités différentes.
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":82,\"idTypeDossier\":\"DAO\",\"idEntiteContract\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLocalite").value("ANT"));
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":83,\"idTypeDossier\":\"DAO\",\"idEntiteContract\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLocalite").value("TMS"));
        // Entité non rattachée à la PRMP → 403.
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":84,\"idTypeDossier\":\"DAO\",\"idEntiteContract\":9}"))
                .andExpect(status().isForbidden());
        // Entité rattachée mais sans localité → 400.
        mvc.perform(post("/api/saisies/dossier").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":85,\"idTypeDossier\":\"DAO\",\"idEntiteContract\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Édition d'un brouillon PPM : en-tête mis à jour + lignes réconciliées (maj/ajout/retrait), mode recalculé")
    void editionPpm_facade() throws Exception {
        natureRepository.save(new Nature(1, "Travaux", null));
        situationRepository.save(new Situation(1, "Normale", null));
        modePassationRepository.save(new ModePassation(1, "AOO", null, null, null, null));
        modePassationRepository.save(new ModePassation(2, "AOR", null, null, null, null));
        modePassationRepository.save(new ModePassation(4, "Cotation", null, null, null, null));
        seuilRepository.save(seuil(901, "ANT", 1, "0", "200000000"));
        seuilRepository.save(seuil(902, "ANT", 1, "200000001", "1000000000"));
        seuilRepository.save(seuil(903, "ANT", 1, "1000000001", null));
        reglePassationRepository.save(regle(901, 1, 901, 4));
        reglePassationRepository.save(regle(902, 1, 902, 2));
        reglePassationRepository.save(regle(903, 1, 903, 1));

        // Saisie initiale : 700 (150M → 4), 701 (500M → 2), entité 1 (ANT).
        String creation = "{\"idDossier\":120,\"idEntiteContract\":1,\"idPpm\":120,\"exercice\":2026,"
                + "\"signataire\":\"RABE\",\"dateSignature\":\"2026-01-10\",\"reference\":\"PPM-120-v1\","
                + "\"marches\":[{\"idDetail\":700,\"montEstim\":150000000,\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"},"
                + "{\"idDetail\":701,\"montEstim\":500000000,\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}]}";
        mvc.perform(post("/api/saisies/ppm").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON).content(creation))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/marches/700").header("Authorization", tokenPrmp)).andExpect(jsonPath("$.idMode").value(4));
        mvc.perform(get("/api/marches/701").header("Authorization", tokenPrmp)).andExpect(jsonPath("$.idMode").value(2));

        // Édition : en-tête + 700→1,5 Md (mode 1), 701 retiré, 702 ajouté (500M → 2).
        String edition = "{\"exercice\":2027,\"signataire\":\"RABE Maj\",\"dateSignature\":\"2026-02-01\",\"reference\":\"PPM-120-v2\","
                + "\"marches\":[{\"idDetail\":700,\"montEstim\":1500000000,\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"},"
                + "{\"idDetail\":702,\"montEstim\":500000000,\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}]}";
        mvc.perform(put("/api/saisies/ppm/120").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON).content(edition))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("BROUILLON"));
        // En-tête mis à jour, lignes réconciliées (700 recalculé→1, 702 créé→2, 701 supprimé→404 en dernier).
        mvc.perform(get("/api/ppms/120").header("Authorization", tokenPrmp))
                .andExpect(jsonPath("$.reference").value("PPM-120-v2"))
                .andExpect(jsonPath("$.exercice").value(2027));
        mvc.perform(get("/api/marches/700").header("Authorization", tokenPrmp)).andExpect(jsonPath("$.idMode").value(1));
        mvc.perform(get("/api/marches/702").header("Authorization", tokenPrmp)).andExpect(jsonPath("$.idMode").value(2));
        mvc.perform(get("/api/marches/701").header("Authorization", tokenPrmp)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Édition de brouillon : gardes — dossier soumis → 409 ; non-propriétaire → 403")
    void editionPpm_gardes() throws Exception {
        String tokenAutrePrmp = bearer("PRMPZZ", ProfilUtilisateur.PRMP, TypeActeur.PRMP, "PRMPZZ", "ANT");
        String edition = "{\"exercice\":2026,\"signataire\":\"X\",\"dateSignature\":\"2026-01-10\",\"reference\":\"R\",\"marches\":[]}";
        // 2 brouillons PPM (sans lignes) de PRMP001, entité 1.
        mvc.perform(post("/api/saisies/ppm").header("Authorization", tokenPrmp).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":121,\"idEntiteContract\":1,\"idPpm\":121,\"exercice\":2026,\"signataire\":\"X\",\"dateSignature\":\"2026-01-10\",\"reference\":\"R121\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/saisies/ppm").header("Authorization", tokenPrmp).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":122,\"idEntiteContract\":1,\"idPpm\":122,\"exercice\":2026,\"signataire\":\"X\",\"dateSignature\":\"2026-01-10\",\"reference\":\"R122\"}"))
                .andExpect(status().isCreated());
        marcheRepository.save(marche(1220, 122, 122)); // un PPM doit comporter au moins un marché avant soumission
        mvc.perform(post("/api/dossiers/122/soumettre").header("Authorization", tokenPrmp)).andExpect(status().isOk());
        // Dossier soumis → non éditable.
        mvc.perform(put("/api/saisies/ppm/122").header("Authorization", tokenPrmp)
                .contentType(MediaType.APPLICATION_JSON).content(edition))
                .andExpect(status().isConflict());
        // Brouillon d'une autre PRMP → 403.
        mvc.perform(put("/api/saisies/ppm/121").header("Authorization", tokenAutrePrmp)
                .contentType(MediaType.APPLICATION_JSON).content(edition))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("File à réceptionner : dossiers SOUMIS de la localité sans réception (Secrétaire) ; cloisonnement et exclusions")
    void fileAReceptionner() throws Exception {
        String tokenSec = bearer("CTRSEC", ProfilUtilisateur.SECRETAIRE, TypeActeur.CONTROLEUR, "CTRSEC", "ANT");
        // SOUMIS ANT sans réception → à réceptionner.
        Dossier a = dossier(110, "SOUMIS"); a.setIdLocalite("ANT"); dossierRepository.save(a);
        // BROUILLON ANT → exclu.
        Dossier b = dossier(111, "BROUILLON"); b.setIdLocalite("ANT"); dossierRepository.save(b);
        // SOUMIS TMS → pas pour le Secrétaire d'ANT.
        Dossier c = dossier(112, "SOUMIS"); c.setIdLocalite("TMS"); dossierRepository.save(c);
        // SOUMIS ANT déjà réceptionné → exclu.
        Dossier d = dossier(113, "SOUMIS"); d.setIdLocalite("ANT"); dossierRepository.save(d);
        receptionRepository.save(reception(113, 113, "CTRSEC", false));

        // Secrétaire d'ANT : seul le 110.
        mvc.perform(get("/api/dossiers/a-receptionner").header("Authorization", tokenSec))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idDossier==110)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==111)]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.idDossier==112)]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.idDossier==113)]", hasSize(0)));
        // Le Président voit toutes les localités (110 ANT + 112 TMS).
        mvc.perform(get("/api/dossiers/a-receptionner").header("Authorization", tokenPresident))
                .andExpect(jsonPath("$[?(@.idDossier==110)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.idDossier==112)]", hasSize(1)));
        // Un Membre n'y a pas accès → 403.
        mvc.perform(get("/api/dossiers/a-receptionner").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Réception interdite si le dossier est en BROUILLON → 409")
    void receptionBrouillon_interdite() throws Exception {
        Dossier d = dossier(67, "BROUILLON");
        d.setIdLocalite("ANT");
        d.setIdPrmp("PRMP001");
        dossierRepository.save(d);
        mvc.perform(post("/api/receptions").header("Authorization", tokenPresident)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idReception\":670,\"idDossier\":67,\"numPassage\":1,\"typePassage\":\"INITIAL\","
                        + "\"imCtrlRecept\":\"CTRPRE\",\"complet\":false}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("KPIs par localité : le CC ne voit que sa localité ; le Président voit tout")
    void kpisParLocalite() throws Exception {
        Dossier a = dossier(140, "SOUMIS"); a.setIdLocalite("ANT"); dossierRepository.save(a);
        Dossier b = dossier(141, "PRET_DISPATCH"); b.setIdLocalite("ANT"); dossierRepository.save(b);
        Dossier c = dossier(142, "SOUMIS"); c.setIdLocalite("TMS"); dossierRepository.save(c);
        Dossier e = dossier(143, "BROUILLON"); e.setIdLocalite("ANT"); dossierRepository.save(e);

        // CC d'ANT : pipeline ANT (SOUMIS, PRET_DISPATCH, BROUILLON), mais nbDossiersSoumis exclut le BROUILLON.
        mvc.perform(get("/api/kpis/tableau-bord").header("Authorization", tokenCc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nbDossiersSoumis").value(2))   // 140, 141 ; le BROUILLON 143 exclu
                .andExpect(jsonPath("$.pipelineParStatut.SOUMIS").value(1))
                .andExpect(jsonPath("$.pipelineParStatut.PRET_DISPATCH").value(1))
                .andExpect(jsonPath("$.pipelineParStatut.BROUILLON").value(1));
        // Président : global → SOUMIS = 2 (140 ANT + 142 TMS).
        mvc.perform(get("/api/kpis/tableau-bord").header("Authorization", tokenPresident))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipelineParStatut.SOUMIS").value(2));
    }

    @Test
    @DisplayName("Publication : workflow EN_ATTENTE → PUBLIE → RETIRE + compteur de consultations")
    void publication_workflow() throws Exception {
        // Création : statut/consultations envoyés ignorés → EN_ATTENTE / 0.
        mvc.perform(post("/api/publications").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPublication\":1,\"typeObjet\":\"PPM\",\"idObjet\":1,"
                        + "\"statutPubli\":\"PUBLIE\",\"nbConsultations\":99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statutPubli").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.nbConsultations").value(0));
        // Publication.
        mvc.perform(post("/api/publications/1/publier").header("Authorization", tokenPublication))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statutPubli").value("PUBLIE"));
        // Consultation (ouverte à tout authentifié) → compteur incrémenté.
        mvc.perform(post("/api/publications/1/consulter").header("Authorization", tokenMembre))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nbConsultations").value(1));
        // Retrait documenté.
        mvc.perform(post("/api/publications/1/retirer").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motifRetrait\":\"Erreur de publication\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statutPubli").value("RETIRE"));
        // Un Membre ne peut pas publier.
        mvc.perform(post("/api/publications/1/publier").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Document public : intégrité SHA-256 (empreinte + vérification)")
    void documentPublic_integriteSha256() throws Exception {
        mvc.perform(post("/api/publications").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idPublication\":1,\"typeObjet\":\"PPM\",\"idObjet\":1}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/document-publics").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDocPublic\":1,\"idPublication\":1,\"libelleDoc\":\"PV\"}"))
                .andExpect(status().isCreated());

        String contenu = Base64.getEncoder().encodeToString("contenu du document".getBytes(StandardCharsets.UTF_8));
        mvc.perform(post("/api/document-publics/1/empreinte").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON).content("{\"contenuBase64\":\"" + contenu + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hashSha256").isNotEmpty());

        // Même contenu → conforme.
        mvc.perform(post("/api/document-publics/1/verifier-integrite").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON).content("{\"contenuBase64\":\"" + contenu + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conforme").value(true));

        // Contenu altéré → non conforme.
        String altere = Base64.getEncoder().encodeToString("contenu altéré".getBytes(StandardCharsets.UTF_8));
        mvc.perform(post("/api/document-publics/1/verifier-integrite").header("Authorization", tokenPublication)
                .contentType(MediaType.APPLICATION_JSON).content("{\"contenuBase64\":\"" + altere + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conforme").value(false));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private org.springframework.test.web.servlet.ResultActions soumettre(String token) throws Exception {
        return mvc.perform(post("/api/pv-examens/1/soumettre").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"imActeur\":\"CTRMEM\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions signer(String token, String acteur, String role)
            throws Exception {
        return mvc.perform(post("/api/pv-examens/1/signer").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imActeur\":\"" + acteur + "\",\"role\":\"" + role + "\"}"));
    }

    private String bearer(String login, ProfilUtilisateur role, TypeActeur type, String ref, String loc) {
        return "Bearer " + tokenService.generer(login, role.name(), type, ref, loc);
    }

    private Localite localite(String id, String libelle) {
        Localite l = new Localite();
        l.setIdLocalite(id);
        l.setLibelleLocalite(libelle);
        l.setReferencement("REF-" + id);
        l.setLocalite(id);
        return l;
    }

    private Profile profile(int id, String libelle) {
        Profile p = new Profile();
        p.setIdProfile(id);
        p.setProfile(libelle);
        return p;
    }

    private Controleur controleur(String im, int profile, String localite) {
        Controleur c = new Controleur();
        c.setImControleur(im);
        c.setNomCont("Nom" + im);
        c.setPrenomsCont("Prenoms");
        c.setEmailCont(im.toLowerCase() + "@cnm.mg");
        c.setIdProfile(profile);
        c.setIdLocalite(localite);
        c.setTransversal(false);
        return c;
    }

    private Prmp prmp(String id, String localite) {
        Prmp p = new Prmp();
        p.setIdPrmp(id);
        p.setNomPrmp("Nom");
        p.setPrenomsPrmp("Prenoms");
        p.setImPrmp("IMP001");
        p.setArreteNomin("ARR-001");
        p.setDateNomin(LocalDate.of(2024, 1, 15));
        p.setCin("101011112222");
        p.setDateCin(LocalDate.of(2010, 5, 5));
        p.setLieuCin("Antananarivo");
        p.setEmailPrmp("prmp@min.mg");
        p.setTelPrmp("0330000001");
        return p;
    }

    private DelegationProfil delegation(int id, int delegant, int delegue) {
        DelegationProfil d = new DelegationProfil();
        d.setIdDelegation(id);
        d.setIdProfileDelegant(delegant);
        d.setIdProfileDelegue(delegue);
        d.setActif(true);
        return d;
    }

    private DemandeRetrait demandeRetrait(int id, int dossier, String idPrmp) {
        DemandeRetrait d = new DemandeRetrait();
        d.setIdDemandeRetrait(id);
        d.setIdDossier(dossier);
        d.setIdPrmp(idPrmp);
        d.setMotifRetrait("Motif de retrait");
        d.setDateDemande(LocalDateTime.of(2026, 6, 5, 10, 0));
        d.setStatut("EN_ATTENTE");
        return d;
    }

    private Ppm ppm(int id, int dossier, String idPrmp) {
        Ppm p = new Ppm();
        p.setIdPpm(id);
        p.setIdDossier(dossier);
        p.setExercice(2026);
        p.setSignataire("Signataire");
        p.setDateSignature(LocalDate.of(2026, 1, 10));
        p.setReference("PPM-REF-" + id);
        p.setIdPrmp(idPrmp);
        return p;
    }

    private Ppm ppmLocalise(int id, int dossier, String localite) {
        Ppm p = new Ppm();
        p.setIdPpm(id);
        p.setIdDossier(dossier);
        p.setExercice(2026);
        p.setSignataire("Signataire");
        p.setDateSignature(LocalDate.of(2026, 1, 10));
        p.setReference("PPM-REF-" + id);
        p.setIdLocalite(localite);
        return p;
    }

    private Marche marche(int idDetail, int dossier, int ppm) {
        Marche m = new Marche();
        m.setIdDetail(idDetail);
        m.setIdDossier(dossier);
        m.setIdPpm(ppm);
        m.setDesignationMarche("Marche " + idDetail);
        m.setStatut("PREVU");
        return m;
    }

    private Ministere ministere(int id) {
        Ministere m = new Ministere();
        m.setIdMinistere(id);
        m.setLibelleMinistere("Ministere " + id);
        return m;
    }

    private Organigramme organigramme(int id, int ministere) {
        Organigramme o = new Organigramme();
        o.setIdOrganigramme(id);
        o.setActif(true);
        o.setIdMinistere(ministere);
        o.setLibelle("Organigramme " + id);
        return o;
    }

    private EntiteContract entite(int id, int organigramme, String localite) {
        EntiteContract e = new EntiteContract();
        e.setIdEntiteContract(id);
        e.setLibelleEntite("Entite " + id);
        e.setAdresse("Adresse");
        e.setIdOrganigramme(organigramme);
        e.setNiveauHierarchique(1);
        e.setIdLocalite(localite);
        return e;
    }

    private PrmpEntite prmpEntite(int id, String prmp, int entite, boolean actif) {
        PrmpEntite pe = new PrmpEntite();
        pe.setIdPrmpEntite(id);
        pe.setIdPrmp(prmp);
        pe.setIdEntiteContract(entite);
        pe.setActif(actif);
        return pe;
    }

    private Avis avis(String id, String libelle) {
        Avis a = new Avis();
        a.setIdAvis(id);
        a.setLibelleAvis(libelle);
        return a;
    }

    private Dossier dossier(int id, String statut) {
        Dossier d = new Dossier();
        d.setIdDossier(id);
        d.setRefeDossier("DOS-" + id);
        d.setDateRef(LocalDate.of(2026, 6, 1));
        d.setStatut(statut);
        return d;
    }

    private Dossier dossierLoc(int id, String statut, String localite, String idPrmp) {
        Dossier d = dossier(id, statut);
        d.setIdLocalite(localite);
        d.setIdPrmp(idPrmp);
        return d;
    }

    private Reception reception(int id, int dossier, String imRecept, boolean complet) {
        Reception r = new Reception();
        r.setIdReception(id);
        r.setIdDossier(dossier);
        r.setNumPassage(1);
        r.setTypePassage("INITIAL");
        r.setImCtrlRecept(imRecept);
        r.setDateReception(LocalDate.of(2026, 6, 2));
        r.setComplet(complet);
        return r;
    }

    private Dispatch dispatch(int id, int reception, String cc, String membre) {
        Dispatch d = new Dispatch();
        d.setIdDispatch(id);
        d.setIdReception(reception);
        d.setImCtrlCc(cc);
        d.setImCtrlMembre(membre);
        d.setDateDispatch(LocalDate.of(2026, 6, 3));
        d.setInterimDispatch(false);
        return d;
    }

    private Examen examen(int id, int dispatch, String membre) {
        Examen e = new Examen();
        e.setIdExamen(id);
        e.setIdDispatch(dispatch);
        e.setImCtrlMembre(membre);
        e.setDateExamen(LocalDate.of(2026, 6, 4));
        return e;
    }
}
