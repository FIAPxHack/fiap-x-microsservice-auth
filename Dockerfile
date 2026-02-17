# Etapa 1 -> Base (Runtime)
FROM eclipse-temurin:17-jre-alpine AS base
WORKDIR /app
EXPOSE 8082

# Etapa 2 -> Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /src

# Copia apenas o pom para aproveitar cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código
COPY src ./src

# Gera o jar
RUN mvn clean package -DskipTests -B

# Etapa 3 -> Final
FROM base AS final
WORKDIR /app

# Copia o jar gerado
COPY --from=build /src/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
