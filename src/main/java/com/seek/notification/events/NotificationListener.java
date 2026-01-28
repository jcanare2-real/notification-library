package com.seek.notification.events;

/**
 * Interfaz para que el cliente reciba actualizaciones de estado.
 * Cumple con el Principio de Inversión de Dependencias (DIP).
 */
@FunctionalInterface
public interface NotificationListener {
    void onEvent(NotificationEvent event);
}