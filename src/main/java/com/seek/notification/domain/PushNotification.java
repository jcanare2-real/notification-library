package com.seek.notification.domain;

import java.util.UUID;

/**
 * Representa una notificación móvil (Push).
 */
public record PushNotification(
        String id,
        String deviceToken,
        String title,
        String body
) implements Notification {

    public PushNotification(String deviceToken, String title, String body) {
        this(UUID.randomUUID().toString(), deviceToken, title, body);
    }
}
