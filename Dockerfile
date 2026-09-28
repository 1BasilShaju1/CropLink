FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN mkdir -p out && javac --add-modules jdk.httpserver -d out $(find src -name "*.java")
EXPOSE 8080
CMD ["java", "--add-modules", "jdk.httpserver", "-cp", "out", "Main"]
