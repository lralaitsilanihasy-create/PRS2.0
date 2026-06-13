package cnm.prs.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Saisie d'un PPM en un seul appel (façade transactionnelle, §3.1 M02) : crée le dossier
 * (type PPM, BROUILLON), le PPM et ses lignes de marché. La PRMP propriétaire est forcée à
 * l'utilisateur courant ; le mode de passation de chaque ligne est déterminé automatiquement.
 */
public record SaisiePpmRequest(

        @NotNull
        Integer idDossier,

        @NotNull
        Integer idEntiteContract,

        @NotNull
        Integer idPpm,

        @NotNull
        Integer exercice,

        @NotBlank
        @Size(max = 50)
        String signataire,

        @NotNull
        LocalDate dateSignature,

        @NotBlank
        @Size(max = 100)
        String reference,

        @Valid
        List<SaisieMarcheLigne> marches) {
}
