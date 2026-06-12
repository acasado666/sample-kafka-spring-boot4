FROM eclipse-temurin:25-jre
WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


#THEN:
#  docker build -t order-events-consumer:v1 .
#  docker run -p 8081:8081 order-events-consumer:v1


#----------------------------------------------------------------------
#FROM maven:3.9-eclipse-temurin-25 AS build
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests
#
#FROM eclipse-temurin:25-jre
#WORKDIR /app
#COPY --from=build /app/target/*.jar app.jar
#EXPOSE 8081
#ENTRYPOINT ["java", "-jar", "app.jar"]

#----------------------------------------------------------------------
#FROM eclipse-temurin:25-jdk AS build
#WORKDIR /workspace
#
#COPY pom.xml ./
#COPY mvnw ./
#COPY .mvn .mvn
#COPY src src
#
#RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests
#
#FROM eclipse-temurin:25-jre
#WORKDIR /app
#
#COPY --from=build /workspace/target/*.jar /app/app.jar
#
#EXPOSE 8081
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]
