# ========================================
# Dockerfile Otimizado - Versão 1: Spring Boot Layers
# Reduz rebuilds usando camadas do Spring Boot
# Suporta múltiplas arquiteturas (amd64, arm64)
# Tamanho estimado: ~300-350MB
# ========================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar apenas pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte e fazer build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Extrair camadas do JAR (melhor cache no Docker)
RUN mkdir -p target/extracted && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Stage 2: Runtime
FROM eclipse-temurin:17-jre AS runtime

# Instalar curl para healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Criar usuário não-root
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copiar camadas do Spring Boot na ordem correta (do menos para o mais mutável)
# Isso permite melhor reuso de cache entre builds
COPY --from=build --chown=spring:spring /app/target/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /app/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /app/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /app/target/extracted/application/ ./

USER spring:spring

EXPOSE 8080

# Healthcheck usando curl (mais leve que wget)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Usar JarLauncher do Spring Boot para carregar as camadas
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "org.springframework.boot.loader.launch.JarLauncher"]

