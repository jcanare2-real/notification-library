package com.seek.notification.validation;

import com.seek.notification.domain.Notification;
import com.seek.notification.exceptions.NotificationValidationException;

/**
 * Interface para estrategias de validación de datos.
 * * Patrón: Strategy.
 * * SOLID: SRP - Cada validador tiene una única razón para cambiar (reglas de su canal).
 */
public interface NotificationValidator<T extends Notification> {
    /**
     * Valida la estructura y datos de la notificación.
     * @throws NotificationValidationException si los datos son inválidos.
     */
    void validate(T notification) throws NotificationValidationException;

    boolean supports(Class<? extends Notification> clazz);
}