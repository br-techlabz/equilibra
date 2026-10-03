# ============================================================
# Equilibra API - Dockerfile
# Multi-stage build para otimizar tamanho da imagem
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar arquivos de configuração do Maven primeiro (cache de dependências)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
# mvnw.cmd não necessário em Linux
# COPY mvnw.cmd .

# Garantir que o wrapper jar existe
COPY .mvn/wrapper/maven-wrapper.jar .mvn/wrapper/maven-wrapper.jar

# Baixar dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src src

# Build da aplicação (skip tests para build mais rápido na imagem)
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -g 1000 -S equilibra && \
    adduser -u 1000 -S equilibra -G equilibra

# Copiar JAR do stage de build
COPY --from=builder /app/target/equilibra-api-*.jar app.jar

# Criar diretório de logs e dar permissão
RUN mkdir -p /app/logs && chown -R equilibra:equilibra /app

# Mudar para usuário não-root
USER equilibra

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health/liveness || exit 1

# Entry point
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]