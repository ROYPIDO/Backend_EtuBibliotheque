package com.openclassrooms.etudiant.handler;

/**
 * Levée quand une ressource demandée n'existe pas (traduite en HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
