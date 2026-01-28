package com.seek.notification.core;

import com.seek.notification.domain.SmsNotification;
import com.seek.notification.events.NotificationListener;
import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.validation.NotificationValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class NotificationManagerTest {

    @Test
    @DisplayName("Debe validar, enviar y notificar evento de éxito")
    void shouldProcessFullFlow() throws Exception {
        // Arrange
        NotificationProvider<SmsNotification> smsProvider = mock(NotificationProvider.class);
        NotificationValidator<SmsNotification> validator = mock(NotificationValidator.class);
        NotificationListener listener = mock(NotificationListener.class);

        when(smsProvider.supports(SmsNotification.class)).thenReturn(true);
        when(smsProvider.getName()).thenReturn("MockSMS");
        when(validator.supports(SmsNotification.class)).thenReturn(true);

        NotificationManager manager = new NotificationManager(
                List.of(smsProvider), List.of(listener), List.of(validator), 1
        );

        SmsNotification sms = new SmsNotification("+51999888777", "Test Msg");

        // Act
        CompletableFuture<Void> future = manager.sendAsync(sms);
        future.join();

        // Assert
        verify(validator).validate(sms); // SRP: Validación ejecutada
        verify(smsProvider).send(sms);    // Strategy: Proveedor correcto usado
        verify(listener).onEvent(any()); // Observer: Listener notificado
    }
}