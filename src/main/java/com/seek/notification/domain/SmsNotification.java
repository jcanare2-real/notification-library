package com.seek.notification.domain;

import java.util.UUID;

/**
 * Representa una notificación de mensaje de texto (SMS).
 */
public record SmsNotification(
        String id,
        String phoneNumber,
        String message
) implements Notification {

    public SmsNotification {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("El número de teléfono es obligatorio");
        }
    }

    public SmsNotification(String phoneNumber, String message) {
        this(UUID.randomUUID().toString(), phoneNumber, message);
    }
}
