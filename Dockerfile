# ---- Stage 1: Build & Test ----
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.14_7_1.10.10_2.13.16 AS builder

# Set the working directory
WORKDIR /app

# Copy build definition and dependencies first for Docker caching
COPY build.sbt .
COPY project ./project

# Fetch dependencies
RUN sbt update

# Copy the full source code
COPY . .

# Compile and run tests
RUN sbt compile test package

# ---- Stage 2: Run ----
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.14_7_1.10.10_2.13.16

# Set the working directory
WORKDIR /app

# Copy built files from the previous stage
COPY --from=builder /app .

# Accept API keys as build arguments and set them as environment variables
ARG MARVEL_API_PRIVATE_KEY
ARG MARVEL_API_PUBLIC_KEY

# Pass arguments to environment variables
ENV MARVEL_API_PRIVATE_KEY=${MARVEL_API_PRIVATE_KEY}
ENV MARVEL_API_PUBLIC_KEY=${MARVEL_API_PUBLIC_KEY}

# Expose any necessary ports
EXPOSE 8080

# Run the Scala application
CMD ["sbt", "run"]
