FROM openjdk:8-jre-alpine

# Run docker in a non-root user by security reasons:
# https://security.stackexchange.com/questions/106860/can-a-root-user-inside-a-docker-lxc-break-the-security-of-the-whole-system
RUN addgroup -S slave && adduser -S slave -G slave
USER slave:slave

# Use the fat jar.
COPY target/*-spring-boot.jar app.jar

# TODO:
#
# Install docker in alpine by copy/pasting
# https://github.com/docker-library/docker/blob/master/19.03/Dockerfile
#
# Docker doesn't support mixin (somthing like `FROM openjdk` and
# `FROM docker`) so not an easy round. Luckily both images are from
# alpine so it should work. Just a lack of tracking upstream updates.

ENTRYPOINT ["java", "-jar", "/app.jar"]
