package cnm.prs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cnm.prs.entity.CompteAuth;

@Repository
public interface CompteAuthRepository extends JpaRepository<CompteAuth, String> {

    Optional<CompteAuth> findByLogin(String login);

    List<CompteAuth> findByRefActeurAndTypeActeur(String refActeur, String typeActeur);

    /** Comptes selon leur état d'activation (ex. {@code false} = en attente de validation). */
    List<CompteAuth> findByActif(Boolean actif);
}
