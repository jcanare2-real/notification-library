package com.seek.notification.exceptions;

/**
 * Indica un fallo técnico al intentar entregar la notificación.
 */
public class NotificationDeliveryException extends NotificationException {
    private final String providerName;

    public NotificationDeliveryException(String providerName, String message, Throwable cause) {
        super(String.format("Fallo en proveedor [%s]: %s", providerName, message), cause);
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}