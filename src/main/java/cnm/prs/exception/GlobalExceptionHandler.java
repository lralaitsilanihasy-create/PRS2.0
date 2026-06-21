package cnm.prs.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;

/**
 * Gestion centralisée des erreurs de l'API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ ResourceNotFoundException.class, EntityNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorResponse.FieldError> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation échouée", request, erreurs);
    }

    /**
     * Identifiant (clé primaire assignée) manquant à la création : les entités du modèle
     * n'auto-génèrent pas leur PK, le client doit la fournir. → 400 plutôt qu'une 500 opaque.
     */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaSystem(JpaSystemException ex, WebRequest request) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.contains("must be manually assigned")) {
            return build(HttpStatus.BAD_REQUEST,
                    "L'identifiant (clé primaire) est obligatoire à la création de cette ressource.",
                    request, null);
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request, null);
    }

    /**
     * Violation d'une contrainte de base : clé primaire en doublon, valeur obligatoire
     * manquante (NOT NULL) ou référence (clé étrangère) inexistante. → 409 plutôt qu'une 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        // Distingue la cause racine (SQLSTATE PostgreSQL) au lieu d'un message fourre-tout.
        String sqlState = sqlState(ex);
        return switch (sqlState == null ? "" : sqlState) {
            case "23503" -> build(HttpStatus.CONFLICT,          // foreign_key_violation
                    "Suppression impossible : cette donnée est référencée par des enregistrements liés.", request, null);
            case "23505" -> build(HttpStatus.CONFLICT,          // unique_violation
                    "Doublon : un enregistrement avec cette clé existe déjà.", request, null);
            case "23502" -> build(HttpStatus.BAD_REQUEST,       // not_null_violation
                    "Valeur obligatoire manquante.", request, null);
            default -> build(HttpStatus.CONFLICT, "Violation d'une contrainte de données.", request, null);
        };
    }

    /** Remonte la chaîne des causes jusqu'à un {@link java.sql.SQLException} pour lire son SQLSTATE. */
    private static String sqlState(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof java.sql.SQLException se) {
                return se.getSQLState();
            }
        }
        return null;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request,
            List<ErrorResponse.FieldError> erreurs) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""),
                erreurs);
        return ResponseEntity.status(status).body(body);
    }
}
