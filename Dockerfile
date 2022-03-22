FROM fabric8/java-alpine-openjdk11-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=wsserver,sink","-Dserver.port=8080","-jar","/app.jar"]