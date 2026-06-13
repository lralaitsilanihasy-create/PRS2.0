package cnm.prs;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.entity.Avis;
import cnm.prs.entity.CompteAuth;
import cnm.prs.entity.Controleur;
import cnm.prs.entity.DelegationProfil;
import cnm.prs.entity.DemandeRetrait;
import cnm.prs.entity.Dispatch;
import cnm.prs.entity.Dossier;
import cnm.prs.entity.Examen;
import cnm.prs.entity.Localite;
import cnm.prs.entity.ModePassation;
import cnm.prs.entity.Nature;
import cnm.prs.entity.Ppm;
import cnm.prs.entity.Prmp;
import cnm.prs.entity.Profile;
import cnm.prs.entity.Reception;
import cnm.prs.entity.ReglePassation;
import cnm.prs.entity.Seuil;
import cnm.prs.entity.Situation;
import cnm.prs.enums.ProfilUtilisateur;
import cnm.prs.enums.TypeActeur;
import cnm.prs.repository.AvisRepository;
import cnm.prs.repository.CompteAuthRepository;
import cnm.prs.repository.ControleurRepository;
import cnm.prs.repository.DelegationProfilRepository;
import cnm.prs.repository.DemandeRetraitRepository;
import cnm.prs.repository.DispatchRepository;
import cnm.prs.repository.DossierRepository;
import cnm.prs.repository.ExamenRepository;
import cnm.prs.repository.LocaliteRepository;
import cnm.prs.repository.ModePassationRepository;
import cnm.prs.repository.NatureRepository;
import cnm.prs.repository.PpmRepository;
import cnm.prs.repository.PrmpRepository;
import cnm.prs.repository.ProfileRepository;
import cnm.prs.repository.ReceptionRepository;
import cnm.prs.repository.ReglePassationRepository;
import cnm.prs.repository.SeuilRepository;
import cnm.prs.repository.SituationRepository;
import cnm.prs.security.TokenService;

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
    @Autowired private DemandeRetraitRepository demandeRetraitRepository;
    @Autowired private DelegationProfilRepository delegationProfilRepository;
    @Autowired private NatureRepository natureRepository;
    @Autowired private SituationRepository situationRepository;
    @Autowired private ModePassationRepository modePassationRepository;
    @Autowired private SeuilRepository seuilRepository;
    @Autowired private ReglePassationRepository reglePassationRepository;

    private String tokenPresident;
    private String tokenCc;
    private String tokenMembre;
    private String tokenAdmin;
    private String tokenPrmp;
    private String tokenPublication;

    @BeforeEach
    void seed() {
        localiteRepository.save(localite("ANT", "Antananarivo"));

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

        String tok = tokenPresident;

        // 1) Création : mode imposé = 2 (AOR). Le idMode=99 envoyé par le client est ignoré.
        //    Localité résolue via PPM 1 → PRMP001 → ANT.
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7001,\"idDossier\":1,\"idPpm\":1,\"montEstim\":500000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"idMode\":99,\"statut\":\"PREVU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMode").value(2));

        // 2) Situation = urgence → mode 3 (Gré à gré), même nature/montant/localité.
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7002,\"idDossier\":1,\"idPpm\":1,\"montEstim\":500000000,"
                        + "\"idNature\":1,\"idSituation\":2,\"statut\":\"PREVU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMode").value(3));

        // 3) Montant hors de toute tranche → aucune règle → idMode null + alerte MODE_NON_DETERMINE.
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7003,\"idDossier\":1,\"idPpm\":1,\"montEstim\":100000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMode").value(nullValue()));
        mvc.perform(get("/api/notifications").header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$[?(@.typeNotif=='MODE_NON_DETERMINE')]", hasSize(1)));

        // 4) Mise à jour : le montant passe à 1,5 Md → recalcul → mode 1 (AOO).
        mvc.perform(put("/api/marches/7001").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDossier\":1,\"idPpm\":1,\"montEstim\":1500000000,"
                        + "\"idNature\":1,\"idSituation\":1,\"statut\":\"PREVU\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMode").value(1));

        // 5) PPM sans PRMP → localité indéterminable → refus 400.
        Ppm sansPrmp = new Ppm();
        sansPrmp.setIdPpm(990);
        sansPrmp.setIdDossier(1);
        sansPrmp.setExercice(2026);
        sansPrmp.setSignataire("TEST");
        sansPrmp.setDateSignature(LocalDate.parse("2026-01-01"));
        sansPrmp.setReference("PPM-TEST-SANS-PRMP");
        ppmRepository.save(sansPrmp);
        mvc.perform(post("/api/marches").header("Authorization", tok).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDetail\":7004,\"idDossier\":1,\"idPpm\":990,\"montEstim\":500000000,"
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
                + "\"telPrmp\":\"0340000000\",\"idLocalite\":\"ANT\"}";

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

        // (c) Examen d'un dossier non PRET_DISPATCH (dispatch 1 → dossier 1 = EN_EXAMEN) → 409.
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
    @DisplayName("Soumission dossier (§3.1, Option C) : la PRMP soumet → référence générée + Secrétaire/CC notifiés ; re-soumission → 409")
    void soumissionDossier_ok() throws Exception {
        // Dossier de la PRMP courante (PPM avec idPrmp = PRMP001) localisé à ANT.
        // refeDossier vide : la référence est générée par la soumission.
        Dossier d = dossier(3, "RECU");
        d.setRefeDossier(null);
        dossierRepository.save(d);
        Ppm ppm = ppmLocalise(30, 3, "ANT");
        ppm.setIdPrmp("PRMP001");
        ppmRepository.save(ppm);

        // Soumission par la PRMP → 200 + référence unique générée.
        mvc.perform(post("/api/dossiers/3/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refeDossier", startsWith("CNM-ANT-2026-")));

        // Le Secrétaire et le CC de la localité sont notifiés.
        mvc.perform(get("/api/notifications").header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$[?(@.typeNotif=='DOSSIER_SOUMIS')].destinataireIm", hasItem("CTRSEC")))
                .andExpect(jsonPath("$[?(@.typeNotif=='DOSSIER_SOUMIS')].destinataireIm", hasItem("CTRCC1")));

        // Re-soumission → 409 (référence déjà générée).
        mvc.perform(post("/api/dossiers/3/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Soumission dossier (§3.1) refus : dossier d'une autre PRMP → 403, PPM sans localité → 400, non-PRMP → 403")
    void soumissionDossier_refus() throws Exception {
        // Dossier NON rattaché à PRMP001 (PPM sans idPrmp), localisé ANT.
        dossierRepository.save(dossier(4, "RECU"));
        ppmRepository.save(ppmLocalise(40, 4, "ANT"));
        // Dossier de PRMP001 mais PPM sans localité ; refeDossier vide pour atteindre le contrôle de localité.
        Dossier d5 = dossier(5, "RECU");
        d5.setRefeDossier(null);
        dossierRepository.save(d5);
        ppmRepository.save(ppm(50, 5, "PRMP001"));

        // (a) Pas son dossier → 403.
        mvc.perform(post("/api/dossiers/4/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isForbidden());
        // (b) PPM sans localité → 400.
        mvc.perform(post("/api/dossiers/5/soumettre").header("Authorization", tokenPrmp))
                .andExpect(status().isBadRequest());
        // (c) Un non-PRMP ne peut pas soumettre → 403.
        mvc.perform(post("/api/dossiers/5/soumettre").header("Authorization", tokenMembre))
                .andExpect(status().isForbidden());
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
        p.setIdLocalite(localite);
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
