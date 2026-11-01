# The PvP server as one container. Build from the repository root:
#   docker build -t battleapp-server .
#   docker run -p 8080:8080 battleapp-server

# Build: the same Gradle build as everywhere else, for the server and the core rules only.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :server:installDist

# Run: just a JRE and the server's own start script and jars.
FROM eclipse-temurin:21-jre
COPY --from=build /src/server/build/install/server /app
# keep the heap inside the machine's memory, whatever size it is
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENV PORT=8080
EXPOSE 8080
USER 1000
CMD ["/app/bin/server"]
