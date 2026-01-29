package com.seek.notification;

import com.seek.notification.config.NotifierBuilder;
import com.seek.notification.core.NotificationManager;
import com.seek.notification.domain.EmailNotification;
import com.seek.notification.domain.SmsNotification;
import com.seek.notification.providers.impl.SendGridEmailProvider;
import com.seek.notification.providers.impl.TwilioSmsProvider;
import com.seek.notification.retry.ExponentialBackoffRetry;
import com.seek.notification.retry.RetryingProviderDecorator;
import lombok.extern.slf4j.Slf4j;

/**
 * DEMO DE EJECUCIÓN (Integration Example).
 * Este Main actúa como un Test de Integración que valida el flujo completo:
 * Builder -> Validation -> Decorator (Retry) -> Async Execution -> Event Observation.
 */
@Slf4j
public class NotificationLibraryDemo {

    public static void main(String[] args) {
        log.info("Iniciando Demo de la Librería de Notificaciones...");

        // 1. Configuración de Políticas de Resiliencia (Decorator Pattern)
        // Definimos un reintento exponencial para el canal de SMS (que suele ser inestable)
        var smsRetryPolicy = new ExponentialBackoffRetry(3, 1000);

        // 2. Orquestación mediante Fluent Builder
        // Inyectamos dependencias manuales para mantener el agnosticismo de frameworks
        NotificationManager manager = new NotifierBuilder()
                // Canal Email: Proveedor directo
                .withProvider(new SendGridEmailProvider("SG.XXXX.API_KEY"))

                // Canal SMS: Proveedor decorado con lógica de reintento
                .withProvider(new RetryingProviderDecorator<>(
                        new TwilioSmsProvider("AC_SID", "AUTH_TOKEN"),
                        smsRetryPolicy
                ))

                // Observabilidad: Listener para auditoría de eventos (Observer Pattern)
                .withEventListener(event -> log.info("[AUDIT EVENT] ID: {} | Status: {} | Provider: {}",
                        event.notificationId(), event.status(), event.providerName()))

                // Tuning de Concurrencia
                .withThreadPoolSize(Runtime.getRuntime().availableProcessors())
                .build();

        // 3. Creación de Notificaciones (Domain Records - Inmutabilidad)
        var welcomeEmail = new EmailNotification(
                "candidate@seek.com",
                "Challenge Completed",
                "Hola, adjunto los entregables solicitados."
        );

        var urgentSms = new SmsNotification("+51999888777", "Su código de acceso es 1234");

        // 4. Ejecución Asíncrona (CompletableFuture)
        // Demostramos el manejo de flujos exitosos y excepcionales
        manager.sendAsync(welcomeEmail)
                .thenRun(() -> log.info(">>> Email procesado correctamente."))
                .exceptionally(ex -> {
                    log.error(">>> Fallo definitivo en Email: {}", ex.getMessage());
                    return null;
                });

        manager.sendAsync(urgentSms)
                .thenAccept(v -> log.info(">>> SMS despachado al pool de ejecución."))
                .exceptionally(ex -> {
                    // Aquí caerían errores de validación Fail-Fast o errores de red tras reintentos
                    log.error(">>> Error en flujo SMS: {}", ex.getMessage());
                    return null;
                });

        // 5. Graceful Shutdown
        // En una aplicación real, esto se ejecutaría al cerrar el contexto (hook)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Liberando recursos de la librería...");
            manager.shutdown();
        }));

        log.info("El hilo principal queda libre para otras tareas mientras las notificaciones se procesan...");
    }
}