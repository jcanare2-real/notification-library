package com.seek.notification.core;

import com.seek.notification.domain.Notification;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.providers.NotificationProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NotificationManager {
    private final List<NotificationProvider<?>> providers;
    private final ExecutorService executorService;

    public NotificationManager(List<NotificationProvider<?>> providers, int poolSize) {
        this.providers = List.copyOf(providers); // Inmutabilidad de la lista de estrategias
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Punto de entrada unificado para el envío de cualquier notificación.
     * Implementa el patrón Strategy para seleccionar el proveedor en runtime.
     */
    @SuppressWarnings("unchecked")
    public <T extends Notification> CompletableFuture<Void> sendAsync(T notification) {
        return CompletableFuture.runAsync(() -> {
            log.info("[AUDIT] Iniciando proceso para notificación ID: {}", notification.id());

            // Buscamos el proveedor que soporte el tipo específico (Email, SMS, etc.)
            NotificationProvider<T> provider = (NotificationProvider<T>) providers.stream()
                    .filter(p -> p.supports(notification.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró un proveedor para: " + notification.getClass().getSimpleName()));

            try {
                provider.send(notification);
                log.info("[AUDIT] Notificación {} enviada con éxito vía {}", notification.id(), provider.getName());
            } catch (NotificationDeliveryException e) {
                log.error("[AUDIT] Error de entrega en {}: {}", provider.getName(), e.getMessage());
                throw new RuntimeException(e); // Envolvemos para CompletableFuture
            }
        }, executorService);
    }

    /**
     * Shutdown ordenado de los recursos de la librería.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}