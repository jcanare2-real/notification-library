package com.seek.notification.retry;

import com.seek.notification.domain.EmailNotification;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.providers.NotificationProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetryingProviderDecoratorTest {

    @Test
    @DisplayName("Debe reintentar 3 veces y finalmente tener éxito")
    void shouldRetryAndSucceed() throws NotificationDeliveryException {
        // Arrange
        NotificationProvider<EmailNotification> mockProvider = mock(NotificationProvider.class);
        EmailNotification notification = new EmailNotification("test@seek.com", "Sub", "Body");
        RetryPolicy policy = new ExponentialBackoffRetry(3, 10); // 10ms para el test

        // Simulamos fallo, fallo, éxito
        doThrow(new NotificationDeliveryException("Error", "Fail", null))
                .doThrow(new NotificationDeliveryException("Error", "Fail", null))
                .doNothing()
                .when(mockProvider).send(notification);

        RetryingProviderDecorator<EmailNotification> decorator =
                new RetryingProviderDecorator<>(mockProvider, policy);

        // Act
        assertDoesNotThrow(() -> decorator.send(notification));

        // Assert: Se debió llamar 3 veces al proveedor decorado
        verify(mockProvider, times(3)).send(notification);
    }
}