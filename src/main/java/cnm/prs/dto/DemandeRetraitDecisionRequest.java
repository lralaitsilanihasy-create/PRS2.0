package cnm.prs.dto;

import jakarta.validation.constraints.Size;

/**
 * Corps (optionnel) d'une décision de retrait. Seul le <strong>refus</strong> peut porter un
 * {@code motif} (facultatif) ; l'acceptation n'a pas de corps. L'identité du décideur provient
 * du JWT, jamais du corps.
 */
public record DemandeRetraitDecisionRequest(@Size(max = 500) String motif) {
}
