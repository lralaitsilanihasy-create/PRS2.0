package cnm.prs.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corps JSON standardisé renvoyé en cas d'erreur.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {
}
