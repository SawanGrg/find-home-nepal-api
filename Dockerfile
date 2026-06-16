FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY mvnw ./
COPY .mvn  .mvn
COPY pom.xml ./

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B --no-transfer-progress

COPY src ./src

RUN ./mvnw package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:17-jdk-jammy AS tester

WORKDIR /app

COPY --from=builder /root/.m2 /root/.m2

COPY mvnw ./
COPY .mvn  .mvn
COPY pom.xml ./
COPY src ./src

RUN chmod +x mvnw

RUN ./mvnw test -B --no-transfer-progress -o

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN apk add --no-cache curl

WORKDIR /app

# just copy the fat JAR directly
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p uploads/images \
             uploads/videos \
             certs \
    && chmod 700 certs

ENV JAVA_OPTS=""

EXPOSE 8080

HEALTHCHECK \
  --interval=30s \
  --timeout=10s \
  --start-period=45s \
  --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]