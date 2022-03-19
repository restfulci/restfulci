FROM tomcat:10-jre11

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/*.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]
