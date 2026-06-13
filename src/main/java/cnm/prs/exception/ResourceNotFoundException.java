package cnm.prs.exception;

/**
 * Levée lorsqu'une ressource demandée n'existe pas. Mappée sur HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
