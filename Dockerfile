FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY mvnw ./
COPY .mvn  .mvn
COPY pom.xml ./

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B --no-transfer-progress

COPY src ./src

RUN ./mvnw package -DskipTests -B --no-transfer-progress

RUN java -Djarmode=layertools \
        -jar target/*.jar \
        extract --destination extracted


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

COPY --from=builder /app/extracted/dependencies          ./
COPY --from=builder /app/extracted/spring-boot-loader    ./
COPY --from=builder /app/extracted/snapshot-dependencies ./
COPY --from=builder /app/extracted/application           ./

RUN mkdir -p uploads/images \
             uploads/videos \
             certs \
    && chmod 700 certs

ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK \
  --interval=30s \
  --timeout=10s \
  --start-period=45s \
  --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]