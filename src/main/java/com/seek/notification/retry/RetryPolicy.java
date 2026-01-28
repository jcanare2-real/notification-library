package com.seek.notification.retry;

/**
 * Interface para definir políticas de reintento.
 * * Patrón: Strategy.
 * * SOLID: SRP - Solo se encarga de calcular tiempos de espera.
 */
public interface RetryPolicy {
    /**
     * Calcula el tiempo de espera antes del siguiente intento.
     * @param attempt Número de intento actual.
     * @return Milisegundos a esperar.
     */
    long getDelay(int attempt);

    int getMaxAttempts();
}