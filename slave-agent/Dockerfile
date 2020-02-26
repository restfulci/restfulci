FROM openjdk:8-jre-alpine

# Run docker in a non-root user by security reasons:
# https://security.stackexchange.com/questions/106860/can-a-root-user-inside-a-docker-lxc-break-the-security-of-the-whole-system
RUN addgroup -S slave && adduser -S slave -G slave
USER slave:slave

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
