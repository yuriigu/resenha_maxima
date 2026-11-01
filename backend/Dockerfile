# ============================================================
# Stage 1: Build (Maven + JDK 27)
# ============================================================
FROM eclipse-temurin:27-jdk AS builder

WORKDIR /build

# Copia apenas o wrapper e o pom para cache de dependências
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Baixa dependências (cache de layer)
RUN ./mvnw dependency:go-offline -B -q

# Copia o código-fonte e compila
COPY src src
RUN ./mvnw package -DskipTests -B -q

# ============================================================
# Stage 2: Runtime (JRE slim)
# ============================================================
FROM eclipse-temurin:27-jre AS runtime

WORKDIR /app

# Copia o JAR gerado no stage anterior
COPY --from=builder /build/target/*.jar app.jar

# Porta exposta pelo Spring Boot
EXPOSE 8080

# Variáveis com valores padrão (podem ser sobrescritas pelo compose/.env)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/device_checker
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=validate

# O depends_on do Compose garante que o DB esteja saudável antes da API iniciar.
# O Flyway executa as migrations automaticamente na inicialização do Spring Boot.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
