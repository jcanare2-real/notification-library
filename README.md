# 🚀 Notification Library

Librería de notificaciones **100% agnóstica de frameworks** (sin Spring/Jakarta), construida con **principios SOLID** y patrones avanzados. Envíos asíncronos resilientes para Email, SMS y Push Notifications.

***

## 🏗️ Arquitectura y Decisiones de Diseño

Diseño escalable y desacoplado:

- **Strategy Pattern**: Despacho dinámico de proveedores por canal
- **Decorator Pattern**: Resiliencia (Retries) sin modificar proveedores base
- **Observer Pattern (Pub/Sub)**: Eventos SENT/FAILED/RETRYING para listeners externos
- **Facade + Fluent Builder**: `NotificationManager` simple vía `NotifierBuilder`
- **Fail-Fast Validation**: Validaciones síncronas antes de procesos async

***

## 📦 Instalación

### Maven

```xml
<dependency>
    <groupId>com.seek.notification</groupId>
    <artifactId>notification-library</artifactId>
    <version>1.0.0</version>
</dependency>
```
***

## ⚡ Quick Start

```java
import com.seek.notification.config.NotifierBuilder;
import com.seek.notification.core.NotificationManager;
import com.seek.notification.domain.EmailNotification;
import com.seek.notification.providers.impl.SendGridEmailProvider;
import com.seek.notification.retry.ExponentialBackoffRetry;

public class Main {
    public static void main(String[] args) {
        // 1. Configurar Manager
        NotificationManager manager = new NotifierBuilder()
            .withProvider(new SendGridEmailProvider("SG.API_KEY"))
            .withEventListener(event -> System.out.println("Audit: " + event.status()))
            .withThreadPoolSize(5)
            .build();

        // 2. Crear notificación inmutable
        var email = new EmailNotification("dev@seek.com", "Challenge", "Arquitectura lista");

        // 3. Envío asíncrono
        manager.sendAsync(email)
            .thenRun(() -> System.out.println("Envío iniciado"))
            .exceptionally(ex -> {
                System.err.println("Error: " + ex.getMessage());
                return null;
            });
    }
}
```

***

## 🔄 Resiliencia con Retry Policy
```java
var retryPolicy = new ExponentialBackoffRetry(3, 1000);
var resilientSms = new RetryingProviderDecorator<>(
    new TwilioSmsProvider("SID", "TOKEN"), 
    retryPolicy
);

managerBuilder.withProvider(resilientSms);
```
***

## 📡 Proveedores Soportados

| Canal | Proveedor | API Real Simulada |
| :-- | :-- | :-- |
| Email | SendGrid v3 | Personalizations + template vars |
| SMS | Twilio | E.164 + MessageSid |
| Push | FCM | Device tokens + payloads |


***

## 📚 API Reference

| Clase | Rol | Patrón |
| :-- | :-- | :-- |
| `NotificationManager` | Orquestador central | Facade |
| `NotifierBuilder` | Configuración fluida | Builder |
| `NotificationProvider<T>` | Interfaz de proveedores | Strategy |
| `NotificationListener` | Suscriptores de eventos | Observer |

### Jerarquía de Excepciones

- `NotificationValidationException`: Errores de datos (fail-fast)
- `NotificationDeliveryException`: Fallos técnicos del proveedor

***

## 🔐 Mejores Prácticas

- **Credenciales**: Inyecta vía Builder desde Secret Manager (no .env)
- **Inmutabilidad**: Usa Java Records para evitar efectos secundarios
- **Fail-Fast**: Valida formatos antes de consumir APIs externas

***

## 🚀 Extensibilidad: Nuevo Canal

1. Crea `Record NewNotification(...) implements Notification`
2. Implementa `NotificationProvider<NewNotification>`
3. (Opcional) `NotificationValidator` para formato específico
4. `.withProvider(new MiProvider(...))`

¡Listo! La librería detectará automáticamente el tipo vía Strategy.

## 🛠️ Ejecución con Docker
Si no tienes Java 21 instalado localmente, puedes compilar y ejecutar la demo completa usando Docker:

1. Construir la imagen:

`docker build -t notification-lib-demo .`

2. Ejecutar la demostración:

`docker run --rm notification-lib-demo`
***