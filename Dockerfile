FROM openjdk:11
COPY target/kitchen-docker.jar kitchen-docker.jar
EXPOSE ${port}
ENTRYPOINT exec java -jar kitchen-docker.jar