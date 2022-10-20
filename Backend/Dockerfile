## Stage 1 : build with maven builder image
# TODO : see if there is a lighter image
FROM quay.io/quarkus/ubi-quarkus-native-image:22.1.0-java11 AS build
COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
USER quarkus
WORKDIR /code
#RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline
RUN ./mvnw install
COPY src /code/src
RUN ./mvnw package -DskipTests

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/openjdk-11:1.11

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=build --chown=185 code/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 code/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 code/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 code/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 185
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"