# Use lightweight Temurin OpenJDK 21 Alpine image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy all repository files
COPY . .

# Find all .java files anywhere in the repository and compile them using @sources.txt
RUN mkdir -p bin && \
    find . -type f -name "*.java" > sources.txt && \
    echo "--- FOUND JAVA FILES ---" && \
    cat sources.txt && \
    javac -d bin @sources.txt

# Expose web server port
EXPOSE 8080

ENV PORT=8080

# Run Main class from compiled bin directory
CMD ["sh", "-c", "java -cp $(find . -name bin -type d | tr '\n' ':'). com.finance.Main"]
