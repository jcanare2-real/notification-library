package com.seek.notification.config;

import com.seek.notification.core.NotificationManager;
import com.seek.notification.events.NotificationListener;
import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.validation.NotificationValidator;
import com.seek.notification.validation.impl.EmailValidator;
import com.seek.notification.validation.impl.SmsValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * MOTOR DE CONFIGURACIÓN (Fluent Builder).
 * * Patrones aplicados:
 * 1. Builder: Facilita la construcción del NotificationManager inyectando todas las piezas.
 * 2. Dependency Injection: Se realiza de forma manual para mantener el agnosticismo.
 * * SOLID:
 * - OCP: El usuario puede añadir sus propios validadores o proveedores sin modificar la lib.
 */
public class NotifierBuilder {
    private final List<NotificationProvider<?>> providers = new ArrayList<>();
    private final List<NotificationListener> listeners = new ArrayList<>();
    private final List<NotificationValidator<?>> validators = new ArrayList<>();
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();

    public NotifierBuilder() {
        // Cargamos validadores por defecto para asegurar la integridad básica
        this.validators.add(new EmailValidator());
        this.validators.add(new SmsValidator());
    }

    /**
     * Registra un proveedor de envío (SendGrid, Twilio, etc.).
     */
    public NotifierBuilder withProvider(NotificationProvider<?> provider) {
        if (provider == null) throw new IllegalArgumentException("Provider no puede ser nulo");
        this.providers.add(provider);
        return this;
    }

    /**
     * Registra un observador para auditoría y tracking de estados.
     */
    public NotifierBuilder withEventListener(NotificationListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener no puede ser nulo");
        this.listeners.add(listener);
        return this;
    }

    /**
     * Permite añadir validadores personalizados o sobreescribir los existentes.
     */
    public NotifierBuilder withValidator(NotificationValidator<?> validator) {
        if (validator == null) throw new IllegalArgumentException("Validator no puede ser nulo");
        this.validators.add(validator);
        return this;
    }

    /**
     * Define el nivel de concurrencia para el envío asíncrono.
     */
    public NotifierBuilder withThreadPoolSize(int size) {
        if (size <= 0) throw new IllegalArgumentException("Pool size debe ser positivo");
        this.threadPoolSize = size;
        return this;
    }

    /**
     * Ensamblaje final.
     * @return Instancia configurada de NotificationManager.
     */
    public NotificationManager build() {
        if (providers.isEmpty()) {
            throw new IllegalStateException("Se requiere al menos un Provider para operar.");
        }

        // Fusión de todas las dependencias en el orquestador
        return new NotificationManager(
                providers,
                listeners,
                validators,
                threadPoolSize
        );
    }
}