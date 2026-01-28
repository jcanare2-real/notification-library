package com.seek.notification.config;

import com.seek.notification.providers.NotificationProvider;
import com.seek.notification.core.NotificationManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de configuración programática para la librería.
 */
public class NotifierBuilder {
    private final List<NotificationProvider<?>> providers = new ArrayList<>();
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * Registra un nuevo proveedor (ej: SendGrid, Mailgun) de forma dinámica.
     */
    public NotifierBuilder withProvider(NotificationProvider<?> provider) {
        if (provider == null) throw new IllegalArgumentException("El proveedor no puede ser nulo");
        this.providers.add(provider);
        return this;
    }

    /**
     * Configura el tamaño del pool para envíos asíncronos.
     */
    public NotifierBuilder withThreadPoolSize(int size) {
        this.threadPoolSize = size;
        return this;
    }

    /**
     * Construye la instancia final del manager con todos los proveedores registrados.
     */
    public NotificationManager build() {
        if (providers.isEmpty()) {
            throw new IllegalStateException("Se debe configurar al menos un proveedor antes de construir el manager");
        }
        return new NotificationManager(providers, threadPoolSize);
    }
}