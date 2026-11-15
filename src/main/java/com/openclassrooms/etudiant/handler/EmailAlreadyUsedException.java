package com.openclassrooms.etudiant.handler;

/**
 * Levée quand l'email d'un étudiant est déjà utilisé par un autre étudiant (traduite en HTTP 409).
 */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String message) {
        super(message);
    }

    public EmailAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
