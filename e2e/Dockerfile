ARG PLAYWRIGHT_VERSION=1.54.2
FROM mcr.microsoft.com/playwright:v${PLAYWRIGHT_VERSION}-jammy

USER root
RUN apt-get update -y \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
         openjdk-21-jdk ca-certificates git \
    && rm -rf /var/lib/apt/lists/*

# Ensure Node version compatible with Vite/@vitejs/plugin-vue engines
RUN npm i -g n && n 20.19.0 && node -v && npm -v

# Pre-install Playwright browsers and system dependencies matching the test runner version
ARG PLAYWRIGHT_VERSION
RUN npx -y @playwright/test@${PLAYWRIGHT_VERSION} install --with-deps

# Keep default Playwright browsers from base image
USER pwuser
WORKDIR /work
