package com.seek.notification.core;

import com.seek.notification.domain.Notification;
import com.seek.notification.events.NotificationEvent;
import com.seek.notification.events.NotificationEvent.NotificationStatus;
import com.seek.notification.events.NotificationListener;
import com.seek.notification.exceptions.NotificationDeliveryException;
import com.seek.notification.exceptions.NotificationValidationException;
import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.validation.NotificationValidator;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ORQUESTADOR CENTRAL (Facade).
 * * Patrones aplicados:
 * 1. Strategy: Selecciona tanto el validador como el proveedor adecuado en runtime.
 * 2. Observer (Pub/Sub): Notifica a interesados externos sobre el éxito o fallo del envío.
 * 3. Facade: Expone una interfaz simple (sendAsync) ocultando la complejidad de hilos, reintentos y validaciones.
 * * SOLID:
 * - SRP: El Manager coordina el flujo. La lógica de "qué es válido" está en el Validator y "cómo enviar" en el Provider.
 * - OCP: Se pueden agregar nuevos canales (ej. Slack) inyectando sus Validadores y Proveedores sin tocar esta clase.
 * - DIP: Depende exclusivamente de abstracciones (NotificationProvider, NotificationListener, NotificationValidator).
 */
@Slf4j
public class NotificationManager {
    private final List<NotificationProvider<?>> providers;
    private final List<NotificationListener> listeners;
    private final List<NotificationValidator<?>> validators;
    private final ExecutorService executorService;

    public NotificationManager(List<NotificationProvider<?>> providers,
                               List<NotificationListener> listeners,
                               List<NotificationValidator<?>> validators,
                               int poolSize) {
        // Inmutabilidad y Seguridad: Defensive Copy para evitar modificaciones externas post-construcción
        this.providers = List.copyOf(providers);
        this.listeners = List.copyOf(listeners);
        this.validators = List.copyOf(validators);
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * Punto de entrada unificado para el envío de notificaciones.
     * * @param notification Objeto inmutable con los datos del mensaje.
     * @return CompletableFuture para manejo asíncrono.
     * @throws NotificationValidationException Si los datos fallan la validación (Fail-Fast).
     */
    @SuppressWarnings("unchecked")
    public <T extends Notification> CompletableFuture<Void> sendAsync(T notification) {
        log.info("[AUDIT] Iniciando proceso para notificación ID: {}", notification.id());

        // 1. FAIL-FAST VALIDATION (Síncrono):
        // Se ejecuta en el hilo del llamador para detectar errores de datos inmediatamente.
        validators.stream()
                .filter(v -> v.supports(notification.getClass()))
                .map(v -> (NotificationValidator<T>) v)
                .forEach(v -> v.validate(notification));

        // 2. DESPACHO ASÍNCRONO:
        // Delegamos a un hilo del pool para no bloquear al cliente durante el IO/Envío.
        return CompletableFuture.runAsync(() -> {

            // OCP: Localización dinámica de la estrategia de envío.
            NotificationProvider<T> provider = (NotificationProvider<T>) providers.stream()
                    .filter(p -> p.supports(notification.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe proveedor configurado para el canal: " + notification.getClass().getSimpleName()));

            try {
                // Ejecución del envío (podría estar decorado con Retries)
                provider.send(notification);

                // Éxito: Notificación de estado (Observer)
                emitEvent(notification.id(), NotificationStatus.SENT, provider.getName(), "Envío completado exitosamente");
                log.info("[AUDIT] Éxito: Notificación {} enviada vía {}", notification.id(), provider.getName());

            } catch (NotificationDeliveryException e) {
                // Fallo técnico: Notificamos el error para trazabilidad y auditoría
                emitEvent(notification.id(), NotificationStatus.FAILED, provider.getName(), e.getMessage());
                log.error("[AUDIT] Error crítico en entrega vía {}: {}", provider.getName(), e.getMessage());

                // Propagamos la excepción para que el CompletableFuture la registre como excepcional
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Publica el estado de la notificación a todos los suscriptores interesados.
     */
    private void emitEvent(String id, NotificationStatus status, String provider, String detail) {
        NotificationEvent event = new NotificationEvent(id, status, provider, Instant.now(), detail);

        listeners.forEach(listener -> {
            try {
                listener.onEvent(event);
            } catch (Exception ex) {
                // Los fallos en auditoría/listeners no deben interrumpir el core de la librería
                log.warn("[AUDIT] Fallo en un suscriptor de eventos: {}", ex.getMessage());
            }
        });
    }

    /**
     * Cierre ordenado de los recursos de la librería.
     */
    public void shutdown() {
        log.info("Cerrando NotificationManager y liberando pool de hilos...");
        executorService.shutdown();
    }
}