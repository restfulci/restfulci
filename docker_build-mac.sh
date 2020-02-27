sed -i'.original' -e "s/spring.profiles.active=local/spring.profiles.active=docker/g" master-api/src/main/resources/application.properties
sed -i'.original' -e "s/spring.profiles.active=local/spring.profiles.active=docker/g" slave-agent/src/main/resources/application.properties

trap_func () {
  sed -i'.original' -e "s/spring.profiles.active=docker/spring.profiles.active=local/g" master-api/src/main/resources/application.properties
  sed -i'.original' -e "s/spring.profiles.active=docker/spring.profiles.active=local/g" slave-agent/src/main/resources/application.properties
}

trap trap_func EXIT

mvn package -Dskip.unittest=true -Dskip.it=true
