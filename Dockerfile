FROM gradle:8.6-jdk8 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
WORKDIR /app
COPY --from=builder /home/gradle/build/libs/fint-altinn-ebevis-service.jar /app/fint-altinn-ebevis-service.jar
CMD ["/app/fint-altinn-ebevis-service.jar"]
