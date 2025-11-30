# syntax=docker/dockerfile:1.7
ARG SYFT_IMAGE=anchore/syft:v1.13.0

FROM node:20 as build-frontend

WORKDIR /app

COPY frontend/package*.json ./

RUN npm ci

# Generate SBOM for frontend dependencies
RUN npx --yes @cyclonedx/cyclonedx-npm -o bom-frontend.cdx.json

COPY frontend ./

RUN npm run build

FROM eclipse-temurin:25-jdk as build-backend

WORKDIR /workspace

RUN apt-get update -y \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*

COPY backend/ ./

COPY --from=build-frontend /app/dist /workspace/src/main/resources/static

RUN chmod +x gradlew

RUN ./gradlew clean bootJar cyclonedxBom -PskipTests

FROM ${SYFT_IMAGE} as generate-sbom
WORKDIR /work
COPY --from=build-backend /workspace/build/libs/*.jar ./app.jar
COPY --from=build-backend /workspace/build/reports/application.cdx.json ./application.cdx.json
COPY --from=build-frontend /app/bom-frontend.cdx.json ./bom-frontend.cdx.json
# anchore/syft is a distroless image where the binary is located at /syft
RUN ["/syft", "dir:.", "-o", "cyclonedx-json=image.cdx.json"]

FROM eclipse-temurin:25-jre as runtime

WORKDIR /opt/goldmanager

# Debian-based Temurin JVM avoids the PaX/Grsecurity mprotect restrictions
# that prevent the Alpine variant from starting on hardened hosts.

COPY --from=build-backend /workspace/build/libs/*.jar /opt/goldmanager/app.jar

COPY --from=build-backend /workspace/build/reports/application.cdx.json /bom/application.cdx.json
COPY --from=build-frontend /app/bom-frontend.cdx.json /bom/frontend.cdx.json

FROM runtime
COPY --from=generate-sbom /work/image.cdx.json /bom/image.cdx.json

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/opt/goldmanager/app.jar"]
