FROM tomcat:8-jre11

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY ./target/master-0.0.1-prototype.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]
