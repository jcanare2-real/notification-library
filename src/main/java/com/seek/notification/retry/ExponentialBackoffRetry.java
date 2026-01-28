package com.seek.notification.retry;

/**
 * Política de reintento con retroceso exponencial.
 */
public record ExponentialBackoffRetry(int maxAttempts, long initialDelay) implements RetryPolicy {

    @Override
    public long getDelay(int attempt) {
        // Fórmula: delay * 2^(attempt-1)
        return initialDelay * (long) Math.pow(2, attempt - 1);
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }
}