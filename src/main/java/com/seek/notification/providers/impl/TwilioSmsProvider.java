package com.seek.notification.providers.impl;

import com.seek.notification.domain.SmsNotification;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwilioSmsProvider implements NotificationProvider<SmsNotification> {
    private final String accountSid;
    private final String authToken;

    public TwilioSmsProvider(String sid, String token) {
        this.accountSid = sid;
        this.authToken = token;
    }

    @Override
    public void send(SmsNotification notification) throws NotificationDeliveryException {
        // Simulación: Twilio POST a /Messages.json
        log.info("[SIMULATION] Twilio Auth con SID: {}", accountSid);
        log.info("[SIMULATION] SMS Enviado a: {} - Msg: {}",
                notification.phoneNumber(), notification.message());

        // Simulación de MessageSid: SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    }

    @Override
    public boolean supports(Class<? extends Notification> clazz) {
        return SmsNotification.class.isAssignableFrom(clazz);
    }

    @Override
    public String getName() { return "Twilio-SMS-Service"; }
}