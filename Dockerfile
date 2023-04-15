FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /appbuild
WORKDIR /appbuild
COPY build/libs/com.example.seefood-backend-all.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]