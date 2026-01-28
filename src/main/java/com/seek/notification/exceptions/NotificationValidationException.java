package com.seek.notification.exceptions;

/**
 * Indica que los datos de la notificación no cumplen con los requisitos.
 * Se extiende de RuntimeException porque representa un error de contrato.
 */
public class NotificationValidationException extends RuntimeException {
    public NotificationValidationException(String message) {
        super(message);
    }
}