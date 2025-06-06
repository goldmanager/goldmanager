FROM node:20 as build-frontend

WORKDIR /app

COPY frontend/package*.json ./

RUN npm install

# Generate SBOM for frontend dependencies
RUN npx --yes @cyclonedx/cyclonedx-npm -o bom-frontend.cdx.json

COPY frontend ./

RUN npm run build

FROM gradle:8-jdk21 as build-backend

WORKDIR /home/gradle/project

COPY backend .

COPY --from=build-frontend /app/dist /home/gradle/project/src/main/resources/static

RUN gradle clean bootJar cyclonedxBom

FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/goldmanager

COPY --from=build-backend /home/gradle/project/build/libs/*.jar /opt/goldmanager/app.jar

COPY --from=build-backend /home/gradle/project/build/reports/application.cdx.json /bom/application.cdx.json
COPY --from=build-frontend /app/bom-frontend.cdx.json /bom/frontend.cdx.json

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/opt/goldmanager/app.jar"]
