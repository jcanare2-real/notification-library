package com.seek.notification.config;

import com.seek.notification.core.NotificationManager;
import com.seek.notification.events.NotificationListener;
import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.retry.RetryPolicy;
import com.seek.notification.retry.RetryingProviderDecorator;

import java.util.ArrayList;
import java.util.List;

/**
 * MOTOR DE CONFIGURACIÓN (Fluent Builder).
 * * Patrones aplicados:
 * 1. Builder: Facilita la creación de un objeto complejo (NotificationManager) paso a paso.
 * * SOLID:
 * - OCP: Permite extender la librería con nuevos providers y listeners sin modificar esta clase.
 */
public class NotifierBuilder {
    private final List<NotificationProvider<?>> providers = new ArrayList<>();
    private final List<NotificationListener> listeners = new ArrayList<>();
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * Registra un proveedor de canal (Email, SMS, Push).
     * Puedes llamar a este método múltiples veces para registrar distintos proveedores.
     */
    public NotifierBuilder withProvider(NotificationProvider<?> provider) {
        if (provider == null) {
            throw new IllegalArgumentException("El proveedor no puede ser nulo");
        }
        this.providers.add(provider);
        return this;
    }

    /**
     * Registra un suscriptor para escuchar los eventos de éxito o fallo (Pub/Sub).
     * Útil para auditoría, métricas o persistencia en base de datos.
     */
    public NotifierBuilder withEventListener(NotificationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("El listener no puede ser nulo");
        }
        this.listeners.add(listener);
        return this;
    }

    /**
     * Configura el tamaño del pool de hilos para el procesamiento asíncrono.
     * Por defecto usa el número de procesadores disponibles.
     */
    public NotifierBuilder withThreadPoolSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño del pool debe ser mayor a cero");
        }
        this.threadPoolSize = size;
        return this;
    }

    /**
     * Construye la instancia final del NotificationManager.
     * Realiza validaciones de integridad antes de la creación.
     */
    public NotificationManager build() {
        if (providers.isEmpty()) {
            throw new IllegalStateException("Se debe configurar al menos un proveedor (Provider) antes de construir el manager");
        }

        // Inyectamos las dependencias coleccionadas al constructor del Manager (Dependency Injection manual)
        return new NotificationManager(providers, listeners, threadPoolSize);
    }

    public NotifierBuilder withResilientProvider(NotificationProvider<?> provider, RetryPolicy policy) {
        // Aplicamos el patrón Decorator antes de guardar el proveedor
        var resilientProvider = new RetryingProviderDecorator<>(provider, policy);
        this.providers.add(resilientProvider);
        return this;
    }
}