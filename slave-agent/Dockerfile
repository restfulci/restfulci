FROM docker:19.03

# Fork from:
# https://github.com/docker-library/openjdk/blob/ee26e3735002dfb83551faf0f3b73822addc4799/15/jdk/alpine/Dockerfile#L1
#
# TODO:
# Use JRE.
ENV JAVA_HOME /opt/openjdk-15
ENV PATH $JAVA_HOME/bin:$PATH

# https://jdk.java.net/
# > Java Development Kit builds, from Oracle
ENV JAVA_VERSION 15-ea+10
ENV JAVA_URL https://download.java.net/java/early_access/alpine/10/binaries/openjdk-15-ea+10_linux-x64-musl_bin.tar.gz
ENV JAVA_SHA256 15a5e8002e24ed129b82bfe55ffe4bdbf3cfd0a7e5ad3399879cdd44175bfd06
# "For Alpine Linux, builds are produced on a reduced schedule and may not be in sync with the other platforms."

RUN set -eux; \
	\
	wget -O /openjdk.tgz "$JAVA_URL"; \
	echo "$JAVA_SHA256 */openjdk.tgz" | sha256sum -c -; \
	mkdir -p "$JAVA_HOME"; \
	tar --extract --file /openjdk.tgz --directory "$JAVA_HOME" --strip-components 1; \
	rm /openjdk.tgz; \
	\
# https://github.com/docker-library/openjdk/issues/212#issuecomment-420979840
# https://openjdk.java.net/jeps/341
	java -Xshare:dump; \
	\
# basic smoke test
	java --version; \
	javac --version

# Run docker in a non-root user by security reasons:
# https://security.stackexchange.com/questions/106860/can-a-root-user-inside-a-docker-lxc-break-the-security-of-the-whole-system
# RUN addgroup -S slave && adduser -S slave -G slave
# USER slave:slave

# Use the fat jar.
# COPY target/*-spring-boot.jar app.jar
COPY slave-agent/target/*-spring-boot.jar app.jar

# TODO:
# Delete it.
RUN apk add maven
RUN mkdir /tmp/code

# TODO:
#
# Install docker in alpine by copy/pasting
# https://github.com/docker-library/docker/blob/master/19.03/Dockerfile
#
# Docker doesn't support mixin (somthing like `FROM openjdk` and
# `FROM docker`) so not an easy round. Luckily both images are from
# alpine so it should work. Just a lack of tracking upstream updates.

ENTRYPOINT ["java", "-jar", "/app.jar"]
