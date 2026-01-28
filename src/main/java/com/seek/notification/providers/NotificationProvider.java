package com.seek.notification.providers;

import com.seek.notification.domain.Notification;
import com.seek.notification.exceptions.NotificationDeliveryException;

/**
 * Contrato base para cualquier implementación de envío.
 * T define el tipo de notificación (Email, SMS, Push) que maneja el proveedor.
 */
public interface NotificationProvider<T extends Notification> {

    /**
     * Ejecuta el envío de la notificación.
     * @param notification Datos inmutables de la notificación.
     * @throws NotificationDeliveryException Si hay un error técnico en el proveedor.
     */
    void send(T notification) throws NotificationDeliveryException;

    /**
     * Determina si este proveedor puede manejar un tipo específico de notificación.
     */
    boolean supports(Class<? extends Notification> clazz);

    /**
     * Nombre único del proveedor (ej. "SendGrid", "Twilio") para auditoría.
     */
    String getName();
}