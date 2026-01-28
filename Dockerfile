# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Instalar dependencias necesarias para mvnw
RUN apk add --no-cache binutils

# Copiar el wrapper de maven y el archivo de proyecto primero para aprovechar el caché de capas
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar solo el artefacto compilado desde la etapa de construcción
COPY --from=build /app/target/*.jar notification-library.jar

# Etiqueta de metadatos
LABEL maintainer="Seek Architect" \
      version="1.0.0" \
      description="Imagen de demo para la librería de notificaciones agnóstica"

# Ejecutar la clase principal de demostración que creamos al inicio
# Nota: Ajustamos el classpath para incluir el JAR generado
ENTRYPOINT ["java", "-cp", "notification-library.jar", "com.seek.notification.NotificationLibraryDemo"]