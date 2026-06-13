package cnm.prs.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cnm.prs.dto.CompteAuthResumeDto;
import cnm.prs.entity.CompteAuth;
import cnm.prs.exception.ResourceNotFoundException;
import cnm.prs.repository.CompteAuthRepository;

/**
 * Gestion des comptes d'authentification par l'Administrateur (validation des inscriptions,
 * réinitialisation des mots de passe).
 */
@Service
@Transactional
public class CompteAuthService {

    private final CompteAuthRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CompteAuthService(CompteAuthRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Comptes en attente de validation (inactifs). */
    @Transactional(readOnly = true)
    public List<CompteAuthResumeDto> enAttente() {
        return repository.findByActif(false).stream().map(this::toDto).toList();
    }

    /** Active un compte (autorise la connexion). */
    public CompteAuthResumeDto activer(String login) {
        CompteAuth compte = load(login);
        compte.setActif(true);
        return toDto(repository.save(compte));
    }

    /** Désactive un compte. */
    public CompteAuthResumeDto desactiver(String login) {
        CompteAuth compte = load(login);
        compte.setActif(false);
        return toDto(repository.save(compte));
    }

    /** Réinitialise le mot de passe d'un compte (action Administrateur). */
    public CompteAuthResumeDto reinitialiserMotDePasse(String login, String nouveauMotDePasse) {
        CompteAuth compte = load(login);
        compte.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        return toDto(repository.save(compte));
    }

    private CompteAuth load(String login) {
        return repository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable : " + login));
    }

    private CompteAuthResumeDto toDto(CompteAuth c) {
        return new CompteAuthResumeDto(c.getLogin(), c.getTypeActeur(), c.getRefActeur(), c.getActif());
    }
}
