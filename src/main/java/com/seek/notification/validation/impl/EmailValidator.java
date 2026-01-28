package com.seek.notification.validation.impl;

import com.seek.notification.domain.EmailNotification;
import com.seek.notification.domain.Notification;
import com.seek.notification.exceptions.NotificationValidationException;
import com.seek.notification.validation.NotificationValidator;

public class EmailValidator implements NotificationValidator<EmailNotification> {
    @Override
    public void validate(EmailNotification n) {
        if (n.to() == null || !n.to().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new NotificationValidationException("Formato de Email inválido para: " + n.to());
        }
        if (n.subject() == null || n.subject().isBlank()) {
            throw new NotificationValidationException("El asunto del correo no puede estar vacío");
        }
    }

    @Override
    public boolean supports(Class<? extends Notification> clazz) {
        return EmailNotification.class.isAssignableFrom(clazz);
    }
}