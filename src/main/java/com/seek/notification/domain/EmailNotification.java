package com.seek.notification.domain;

import java.util.Map;
import java.util.UUID;

/**
 * Representa una notificación de correo electrónico.
 */
public record EmailNotification(
        String id,
        String to,
        String subject,
        String body,
        Map<String, String> templateVariables
) implements Notification {

    // Constructor compacto para validación inicial (Fail-fast)
    public EmailNotification {
        if (to == null || !to.contains("@")) {
            throw new IllegalArgumentException("Dirección de correo inválida");
        }
    }

    // Facilidad para crear con ID automático
    public EmailNotification(String to, String subject, String body) {
        this(UUID.randomUUID().toString(), to, subject, body, Map.of());
    }
}