# build stage: use official Maven image
FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /workspace/app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B
RUN java -Djarmode=layertools -jar target/*.jar extract

# runtime stage: Eclipse Temurin JRE on Alpine
FROM eclipse-temurin:21-jre-alpine

# install tzdata to copy the required timezone file, then remove it to reduce attack surface
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Africa/Nairobi /etc/localtime && \
    echo "Africa/Nairobi" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

# create non-root user and group using Alpine busybox builtins — no shadow package required
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# copy application layers
COPY --from=builder --chown=appuser:appgroup /workspace/app/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/app/application/ ./

# set timezone — resolved from /etc/localtime, not from tzdata package
ENV TZ=Africa/Nairobi

# expose application port
EXPOSE 8093

# switch to non-root user
USER appuser

# healthcheck — uses busybox wget, no curl package required
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8093/ebikes-routing/actuator/health || exit 1

# run the application using spring boot layertools launcher
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]

# static labels — identity and licensing only
# dynamic labels (version, created, revision, source) are injected by the CI/CD pipeline
LABEL org.opencontainers.image.title="eBikes Africa Routing & Pricing Service"
LABEL org.opencontainers.image.description="Route orchestration and deterministic pricing quotes for the eBikes Africa dispatch platform"
LABEL org.opencontainers.image.vendor="eBikes Africa"
LABEL org.opencontainers.image.licenses="Proprietary"
LABEL org.opencontainers.image.base.name="eclipse-temurin:21-jre-alpine"