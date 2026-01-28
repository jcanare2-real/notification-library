package com.seek.notification.core;

import com.seek.notification.domain.Notification;
import com.seek.notification.events.NotificationEvent;
import com.seek.notification.events.NotificationEvent.NotificationStatus;
import com.seek.notification.events.NotificationListener;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.providers.NotificationProvider;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ORQUESTADOR CENTRAL (Facade).
 * * Patrones aplicados:
 * 1. Strategy: Selecciona el proveedor adecuado en tiempo de ejecución.
 * 2. Observer (Pub/Sub): Notifica a interesados externos sobre el estado del envío.
 * 3. Facade: Expone una interfaz simple (sendAsync) ocultando la complejidad multihilo.
 * * SOLID:
 * - SRP: Solo coordina el flujo. No sabe CÓMO se envía (Provider) ni QUÉ se hace con el resultado (Listener).
 * - DIP: Depende de interfaces (NotificationProvider, NotificationListener), no de implementaciones.
 */
@Slf4j
public class NotificationManager {
    private final List<NotificationProvider<?>> providers;
    private final List<NotificationListener> listeners;
    private final ExecutorService executorService;

    public NotificationManager(List<NotificationProvider<?>> providers,
                               List<NotificationListener> listeners,
                               int poolSize) {
        // Inmutabilidad (Defensive Copy): Garantiza que la configuración no cambie post-arranque
        this.providers = List.copyOf(providers);
        this.listeners = List.copyOf(listeners);
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Punto de entrada unificado.
     * @param <T> Tipo que extiende de Notification (Email, Sms, Push).
     */
    @SuppressWarnings("unchecked")
    public <T extends Notification> CompletableFuture<Void> sendAsync(T notification) {
        return CompletableFuture.runAsync(() -> {
            log.info("[AUDIT] Procesando notificación ID: {}", notification.id());

            // OCP: El Manager no tiene IFs por canal. Usa el método supports() de la estrategia.
            NotificationProvider<T> provider = (NotificationProvider<T>) providers.stream()
                    .filter(p -> p.supports(notification.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe proveedor configurado para: " + notification.getClass().getSimpleName()));

            try {
                // Ejecución de la estrategia de envío
                provider.send(notification);

                // Éxito: Notificamos a los suscriptores (Observer)
                emitEvent(notification.id(), NotificationStatus.SENT, provider.getName(), "Envío exitoso");

                log.info("[AUDIT] Éxito: Notificación {} vía {}", notification.id(), provider.getName());

            } catch (NotificationDeliveryException e) {
                // Error técnico: Notificamos el fallo para auditoría externa
                emitEvent(notification.id(), NotificationStatus.FAILED, provider.getName(), e.getMessage());

                log.error("[AUDIT] Fallo en {}: {}", provider.getName(), e.getMessage());
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Encapsula la lógica de notificación a observadores.
     * Garantiza que un fallo en un Listener no rompa el flujo principal.
     */
    private void emitEvent(String id, NotificationStatus status, String provider, String detail) {
        NotificationEvent event = new NotificationEvent(id, status, provider, Instant.now(), detail);

        listeners.forEach(listener -> {
            try {
                listener.onEvent(event);
            } catch (Exception ex) {
                log.warn("[AUDIT] Un suscriptor falló al procesar el evento: {}", ex.getMessage());
            }
        });
    }

    /**
     * Graceful Shutdown: Asegura que las tareas pendientes terminen antes de cerrar la lib.
     */
    public void shutdown() {
        log.info("Cerrando NotificationManager...");
        executorService.shutdown();
    }
}