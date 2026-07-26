FROM maven:4.0.0-rc-5-eclipse-temurin-25-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
# copy built jar (support wildcard for artifact name)
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9200
ENTRYPOINT ["java","-jar","/app/app.jar"]
