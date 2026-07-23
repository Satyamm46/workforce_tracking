# syntax=docker/dockerfile:1

# =========================================================
#  Build stage — compile and package the Spring Boot jar
#  using the bundled Maven wrapper on a full JDK 24 image.
# =========================================================
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

# Copy the wrapper + POM first so dependency resolution is cached
# as its own layer and only re-runs when pom.xml changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Now copy sources and build the executable jar (skip tests in the image;
# tests need a DB and are run in CI, not during the deploy build).
COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# =========================================================
#  Runtime stage — slim JRE image that only carries the jar.
# =========================================================
FROM eclipse-temurin:24-jre
WORKDIR /app

# Run as a non-root user (good practice; Render/containers allow it).
RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

# The app reads server.port from ${PORT} (Render injects it); 8080 locally.
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
