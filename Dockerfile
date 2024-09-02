FROM node:latest as build-frontend

WORKDIR /app

COPY frontend/package*.json ./

RUN npm install

COPY frontend ./

RUN npm run build

FROM gradle:latest as build-backend

WORKDIR /home/gradle/project

COPY backend .

COPY --from=build-frontend /app/dist /home/gradle/project/src/main/resources/static

RUN gradle clean bootJar

FROM anchore/syft:latest as sbom-generator
COPY --from=build-backend /home/gradle/project/build/libs/*.jar /app.jar
USER root:root
RUN syft /app.jar -o json > /sbom.json

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /opt/goldmanager

COPY --from=build-backend /home/gradle/project/build/libs/*.jar /opt/goldmanager/app.jar
COPY --from=sbom-generator /sbom.json /opt/goldmanager/sbom.json
RUN chmod +r /opt/goldmanager -R
RUN chmod 444 /opt/goldmanager/app.jar 
    
USER spring:spring

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/home/spring/app.jar"]
