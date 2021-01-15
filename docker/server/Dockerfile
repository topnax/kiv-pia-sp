FROM gradle:6.6.1-jdk8
# FROM openjdk:8-jre-alpine



USER root                

ENV APPLICATION_USER ktor
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app


# COPY ./server/build/libs/tictactoe-server.jar /app/tictactoe-server.jar
COPY ./server /app
WORKDIR /app

USER root                
RUN chown -R gradle . 
USER gradle              


RUN gradle build --stacktrace

USER $APPLICATION_USER
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "/build/libs/tictactoe-server.jar"]

