package cnm.prs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité JPA mappée sur la table {@code t_prmp}.
 * Générée à partir du MLD (db_ppm110626.pgerd).
 */
@Entity
@Table(name = "t_prmp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prmp {

    @Id
    @Column(name = "ID_PRMP", nullable = false, length = 10)
    private String idPrmp;

    @Column(name = "NOM_PRMP", nullable = false, length = 50)
    private String nomPrmp;

    @Column(name = "PRENOMS_PRMP", nullable = false, length = 100)
    private String prenomsPrmp;

    @Column(name = "IM_PRMP", nullable = false, length = 6)
    private String imPrmp;

    @Column(name = "ARRETE_NOMIN", nullable = false, length = 100)
    private String arreteNomin;

    @Column(name = "DATE_NOMIN", nullable = false)
    private LocalDate dateNomin;

    @Column(name = "CIN", nullable = false, length = 12)
    private String cin;

    @Column(name = "DATE_CIN", nullable = false)
    private LocalDate dateCin;

    @Column(name = "LIEU_CIN", nullable = false, length = 50)
    private String lieuCin;

    @Column(name = "EMAIL_PRMP", nullable = false, length = 100)
    private String emailPrmp;

    @Column(name = "TEL_PRMP", nullable = false, length = 20)
    private String telPrmp;

    // Déprécié : la PRMP n'a plus de localité propre (son périmètre est la propriété des dossiers,
    // et la localité d'un dossier vient de l'entité contractante). Colonne rendue nullable avant
    // suppression définitive (cf. migration ID_LOCALITE). N'est plus écrite ni lue par la logique métier.
    @Column(name = "ID_LOCALITE", length = 5)
    private String idLocalite;
}
