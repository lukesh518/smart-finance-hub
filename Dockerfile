# Use lightweight Temurin OpenJDK 21 Alpine image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy project files into container
COPY . .

# Dynamically find and compile all Java source files into bin directory
RUN mkdir -p bin && javac -d bin $(find . -name "*.java")

# Expose web server port
EXPOSE 8080

# Environment variable for Cloud Port binding
ENV PORT=8080

# Run the Java Web Application
CMD ["java", "-cp", "bin", "com.finance.Main"]
