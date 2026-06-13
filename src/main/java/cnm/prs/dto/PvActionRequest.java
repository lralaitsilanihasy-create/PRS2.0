package cnm.prs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de requête commun aux actions de workflow du projet de PV
 * (soumettre / retourner / accepter / signer).
 *
 * <p>{@code imActeur} identifie temporairement l'acteur de l'action ; il sera
 * remplacé par l'utilisateur authentifié (principal JWT) au Lot 4 — sécurité.</p>
 *
 * @param imActeur    matricule du contrôleur qui exécute l'action (NOT NULL)
 * @param commentaire commentaire ; obligatoire pour un retour de rectification (§3.2)
 * @param role        rôle du signataire (MEMBRE / PRESIDENT / CC) — uniquement pour « signer »
 */
public record PvActionRequest(

        @NotBlank
        @Size(max = 7)
        String imActeur,

        String commentaire,

        @Size(max = 20)
        String role) {
}
