package com.seek.notification.validation.impl;

import com.seek.notification.domain.Notification;
import com.seek.notification.domain.SmsNotification;
import com.seek.notification.exceptions.NotificationValidationException;
import com.seek.notification.validation.NotificationValidator;

public class SmsValidator implements NotificationValidator<SmsNotification> {
    @Override
    public void validate(SmsNotification n) {
        // Validación basada en estándar E.164 requerido por Twilio/AWS SNS
        if (n.phoneNumber() == null || !n.phoneNumber().matches("^\\+[1-9]\\d{1,14}$")) {
            throw new NotificationValidationException("Número de teléfono debe estar en formato E.164 (ej: +51999...)");
        }
    }

    @Override
    public boolean supports(Class<? extends Notification> clazz) {
        return SmsNotification.class.isAssignableFrom(clazz);
    }
}