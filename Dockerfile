FROM openjdk:11
CMD ["gradle", "clean", "build"]
COPY ./build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]