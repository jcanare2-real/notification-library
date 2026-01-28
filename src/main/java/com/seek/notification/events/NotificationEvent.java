package com.seek.notification.events;

import com.seek.notification.domain.Notification;
import java.time.Instant;

/**
 * Representa un cambio de estado en el ciclo de vida de una notificación.
 */
public record NotificationEvent(
        String notificationId,
        NotificationStatus status,
        String providerName,
        Instant timestamp,
        String details // Mensaje de éxito o error técnico
) {
    public enum NotificationStatus {
        SENT, FAILED, RETRYING
    }
}