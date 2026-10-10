# Multi-stage Dockerfile for NAMITHAMART
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY . .

# Compile Java sources
RUN mkdir -p bin && \
    javac -cp "lib/gson.jar:lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" \
    -d bin \
    src/main/java/com/namithamart/Main.java \
    src/main/java/com/namithamart/Database.java \
    src/main/java/com/namithamart/models/*.java \
    src/main/java/com/namithamart/utils/*.java \
    src/main/java/com/namithamart/controllers/*.java

# Runtime container
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy compiled classes, libraries, and public web assets
COPY --from=builder /app/bin ./bin
COPY --from=builder /app/lib ./lib
COPY --from=builder /app/public ./public

# Render port default
ENV PORT=10000
EXPOSE 10000

# Start NAMITHAMART server
CMD ["java", "-cp", "bin:lib/gson.jar:lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar", "com.namithamart.Main"]
