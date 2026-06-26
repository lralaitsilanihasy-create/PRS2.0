package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de transfert pour {@link cnm.prs.entity.Reception}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionDto {

    /** PK technique de la réception — <strong>allouée par le serveur</strong> (séquence) ; ignorée si fournie
     *  en entrée. Présente en sortie (référencée notamment par le dispatch). */
    private Integer idReception;

    @NotNull
    private Integer idDossier;

    @NotNull
    private Integer numPassage;

    @NotBlank
    @Size(max = 10)
    private String typePassage;

    @Size(max = 7)
    private String imCtrlRecept;

    /** Date et heure de réception, formatée {@code yyyy-MM-dd HH:mm} (lecture seule en sortie). */
    private String dateReception;

    /** Date et heure de soumission du dossier, {@code yyyy-MM-dd HH:mm} — lecture seule ; {@code null}
     *  pour un dossier ancien sans date de soumission enregistrée. */
    private String dateSoumission;

    @Size(max = 500)
    private String observation;

    private Boolean complet;

    private Integer idReceptionPrec;

    /** (Règle ajoutée) Référence officielle générée à la réception — lecture seule (non stockée sur t_reception). */
    private String reference;
}
