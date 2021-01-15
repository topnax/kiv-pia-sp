# kiv-pia-sp
A repository for the KIV/PIA semester project (on-line version of the 'Piškvorky' game, also known as *Five in a row*).

## Technology stack

### Server
- consists of a JVM application, [MariaDB](https://mariadb.org/) database and [redis](https://redis.io/)

#### Application
- written in the Kotlin language 
- using [ktor](https://ktor.io/) as the framework to create an HTTP server
- using [Koin](https://insert-koin.io/) as a DI framework
- using [Exposed](https://github.com/JetBrains/Exposed) as an ORM framework
- consists of following services:
  - `GameService` - handles all game logic (player turns, creation of a game,...)
  - `GameMessagingService` - used by `GameService` to be able to send messages to players over websockets
  - `LobbyService` - implements the logic required to run a lobby system
  - `LobbyMessagingService` -  used by `LobbyService` to be able to send messages to lobby members and other users over websockets
  - `ConfigurationService` - makes parameters in the `application.conf` file available to other services in the application
  - `FriendService` - takes care of friend-list listing, friend requests, friend relationships,...
  - `HashService` - defines `hashPassword` that outputs the hash of the input
  - `NotificationService` - allows other services to send notifications to users in real-time over websockets
  - `RealtimeService` - allows other services to send simple messages to connected users over websockets
  - `UserService` - handles user authentication, registration,...
  

#### Persistent database
- **MariaDB** database is used to store user registrations, user friend-list, pending friend requests, results of all games played as well as turns made in those games.
- the database structure is defined in `./server/src/database/Schema.kt`

#### In-memory database
- **redis** database is used to store the list of currently logged-in users
- this gives the possibility to horizontally scale the app in the future
    - the main reason was to learn/try-out **redis** database
  
### Client
- written purely in Javascript
- uses [Nuxt.js](https://nuxtjs.org/) frontend framework, that wraps and simplifies [Vue.js](https://vuejs.org/) usage
- [Vuetify](https://vuetifyjs.com) library used to create the UI (easy to use, implements Material Design)
- [Vuex](https://vuex.vuejs.org/) as the state management framework to separate module logic
    
## Deployment notes
In order to try out the whole application (including both server and client) run `docker-compose up` in the root of this repository.
  - execute `docker-compose up` in the `./server` folder in order to run the server without the client
