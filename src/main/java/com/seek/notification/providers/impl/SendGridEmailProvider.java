package com.seek.notification.providers.impl;

import com.seek.notification.domain.EmailNotification;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendGridEmailProvider implements NotificationProvider<EmailNotification> {
    private final String apiKey;

    public SendGridEmailProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void send(EmailNotification notification) throws NotificationDeliveryException {
        // Simulación: La API v3 de SendGrid espera un JSON con 'personalizations' y 'content'
        log.info("[SIMULATION] Conectando a api.sendgrid.com con API Key: SG.***");
        log.info("[SIMULATION] Payload: To: {}, Subject: {}, Variables: {}",
                notification.to(), notification.subject(), notification.templateVariables());

        // Simulación de respuesta 202 Accepted
    }

    @Override
    public boolean supports(Class<? extends Notification> clazz) {
        return EmailNotification.class.isAssignableFrom(clazz);
    }

    @Override
    public String getName() { return "SendGrid-API-v3"; }
}