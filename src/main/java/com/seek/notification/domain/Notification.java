package com.seek.notification.domain;

/**
 * Abstracción unificada para todos los canales de notificación.
 * Cumple con el requerimiento de interfaz única para Email, SMS y Push.
 */
public sealed interface Notification
        permits EmailNotification, SmsNotification, PushNotification {

    // Método común opcional para auditoría o tracking
    String id();
}