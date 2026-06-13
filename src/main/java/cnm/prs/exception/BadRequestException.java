package cnm.prs.exception;

/**
 * Levée lorsqu'une donnée fournie par le client est invalide au regard d'une règle métier
 * (hors validation @Valid). Traduite en HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
