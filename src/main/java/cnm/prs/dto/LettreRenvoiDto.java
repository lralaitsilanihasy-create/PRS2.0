package cnm.prs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.LettreRenvoi}.
 * {@code idDossier}, {@code refLettre}, {@code dateExamen}, {@code dateLettre}, {@code statut} et
 * {@code imSignataire} sont posés/dérivés par le serveur (lecture seule ; ignorés en entrée).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LettreRenvoiDto {

    private Integer idLettre;

    @NotNull(message = "L'examen est obligatoire.")
    private Integer idExamen;

    private Integer idDossier;

    private String refLettre;

    @NotNull(message = "L'objet de la lettre est obligatoire.")
    @Size(max = 500)
    private String objetLettre;

    /** Corps libre de la lettre (TEXT, nullable, sans contrainte de taille). */
    private String corpsLettre;

    private LocalDate dateExamen;

    private LocalDate dateLettre;

    private String statut;

    private String imSignataire;

    /** Nom complet du signataire (« prénoms nom »), peuplé serveur en lecture (lecture seule). */
    private String nomSignataire;
}
