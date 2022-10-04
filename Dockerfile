FROM openjdk:11
EXPOSE 8081
ADD target/kitchen-docker.jar kitchen-docker.jar
ENTRYPOINT ["java","-jar","kitchen-docker.jar"]