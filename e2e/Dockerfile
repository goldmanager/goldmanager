ARG PLAYWRIGHT_VERSION=1.54.2

FROM eclipse-temurin:25-jdk AS temurin

FROM mcr.microsoft.com/playwright:v${PLAYWRIGHT_VERSION}-jammy
ARG PLAYWRIGHT_VERSION

USER root
COPY --from=temurin /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN apt-get update -y \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
         ca-certificates git \
    && rm -rf /var/lib/apt/lists/*

# Ensure Node version compatible with Vite/@vitejs/plugin-vue engines
RUN npm i -g n && n 20.19.0 && node -v && npm -v

# Pre-install Playwright browsers and system dependencies matching the test runner version
RUN npx -y @playwright/test@${PLAYWRIGHT_VERSION} install --with-deps

# Keep default Playwright browsers from base image
USER pwuser
WORKDIR /work
