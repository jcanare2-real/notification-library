package com.seek.notification.retry;

import com.seek.notification.domain.Notification;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.providers.NotificationProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * DECORADOR DE RESILIENCIA.
 * * Patrón: Decorator. Encapsula un NotificationProvider para añadirle reintentos.
 * * SOLID:
 * - OCP: Añade funcionalidad sin tocar el código del proveedor original.
 * - LSP: Sigue siendo un NotificationProvider, por lo que puede usarse donde sea que se pida uno.
 */
@Slf4j
public class RetryingProviderDecorator<T extends Notification> implements NotificationProvider<T> {

    private final NotificationProvider<T> decoratedProvider;
    private final RetryPolicy retryPolicy;

    public RetryingProviderDecorator(NotificationProvider<T> provider, RetryPolicy policy) {
        this.decoratedProvider = provider;
        this.retryPolicy = policy;
    }

    @Override
    public void send(T notification) throws NotificationDeliveryException {
        int attempts = 0;
        while (attempts < retryPolicy.getMaxAttempts()) {
            try {
                attempts++;
                decoratedProvider.send(notification);
                return; // Éxito: salimos del bucle
            } catch (NotificationDeliveryException e) {
                if (attempts >= retryPolicy.getMaxAttempts()) {
                    log.error("[RETRY] Se agotaron los {} intentos para el proveedor {}",
                            retryPolicy.getMaxAttempts(), getName());
                    throw e; // Relanzamos si ya no hay más intentos
                }

                long delay = retryPolicy.getDelay(attempts);
                log.warn("[RETRY] Intento {} fallido en {}. Reintentando en {}ms...",
                        attempts, getName(), delay);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new NotificationDeliveryException(getName(), "Hilo de reintento interrumpido", ie);
                }
            }
        }
    }

    @Override
    public boolean supports(Class<? extends Notification> clazz) {
        return decoratedProvider.supports(clazz);
    }

    @Override
    public String getName() {
        return decoratedProvider.getName() + " (With Retry)";
    }
}