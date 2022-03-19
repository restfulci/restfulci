FROM docker:19.03

RUN apk update && \
    apk add --update openjdk11-jre && \
    apk add --update git && \
    apk add --update openssh

# Currently can only download public repo by HTTP protocol:
# > git clone https://github.com/restfulci/restfulci-examples.git
# SSH protocol fails by permission error.
# > $ git clone git@github.com:restfulci/restfulci-examples.git
# > git@github.com: Permission denied (publickey).
# > fatal: Could not read from remote repository.
# Dummy key doesn't work unless you give this public key to
# GitHub. That include public repo.
#
# TODO:
# SSH agent forwarding. Doesn't work for mac naively.
# At least need to find out a way to make it work for Kubernetes
# (should be able to setup host machines as Linux).
#
# RUN cd ~ && ssh-keygen -t rsa -b 4096 -f $HOME/.ssh/id_rsa -N "" -C "my@email.com" && \
#     git config --global user.name "My Name" && \
#     git config --global user.email "my@email.com" && \
#     ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts

# Use the fat jar.
COPY target/*-spring-boot.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
