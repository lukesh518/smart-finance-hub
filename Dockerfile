# Use lightweight Temurin OpenJDK 21 Alpine image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy project files into container
COPY . .

# Compile Java source code into bin directory
RUN javac -d bin -sourcepath src src/com/finance/Main.java

# Expose web server port
EXPOSE 8080

# Environment variable for Cloud Port binding
ENV PORT=8080

# Run the Java Web Application
CMD ["java", "-cp", "bin", "com.finance.Main"]
