package com.seek.notification.exceptions;

/**
 * Excepción raíz para todos los errores de la librería.
 * Proporciona información clara sobre el fallo
 */
public abstract class NotificationException extends Exception {
    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}