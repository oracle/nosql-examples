FROM ghcr.io/graalvm/jdk:ol8-java17
EXPOSE 8000
ARG JAR_FILE=target/spring-data-rest-nosql-0.0.1.jar
ADD ${JAR_FILE} spring-data-rest-nosql-0.0.1.jar
RUN ls -aldrt
ENTRYPOINT ["java","-jar","./spring-data-rest-nosql-0.0.1.jar"]
