FROM docker:19.03

RUN apk add openjdk8-jre

# Use the fat jar.
COPY target/*-spring-boot.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
