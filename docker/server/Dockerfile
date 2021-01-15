FROM gradle:6.6.1-jdk8

# switch to root so we are able to create a new user
USER root                

ENV APPLICATION_USER ktor
RUN adduser --no-create-home $APPLICATION_USER

# create a folder for the server
RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

# copy sources of the server to the app folder
COPY ./server /app

# set the app folder as a WD
WORKDIR /app

# switch to root so we can grant gradle user permissions to this folder
USER root                

RUN chown -R gradle . 

# switch back to the gradle user
USER gradle              

# perform gradle build
RUN gradle build --stacktrace


# switch to the application user and run the server
USER $APPLICATION_USER
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "./build/libs/tictactoe-server.jar"]

