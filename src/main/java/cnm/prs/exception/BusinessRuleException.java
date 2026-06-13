package cnm.prs.exception;

/**
 * Levée lorsqu'une règle de gestion (transition d'état interdite, contrainte
 * métier non respectée…) empêche d'exécuter une action. Traduite en HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
