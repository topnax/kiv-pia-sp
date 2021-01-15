# kiv-pia-sp
A repository for the KIV/PIA semester project (on-line version of the 'Piškvorky' game, also known as *Five in a row*).

## Technology stack

### Server
- consists of a JVM application, [MariaDB](https://mariadb.org/) database and [redis](https://redis.io/)
- the server API is documented [here](https://github.com/topnax/kiv-pia-sp/blob/master/server/openapi.yml)
  - feel free to paste it into the [swagger editor](https://editor.swagger.io/)

#### Application
- written in the Kotlin language
- using [Gradle](https://gradle.org/) build tool
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
- JWT token used for user authentication

#### Persistent database
- **MariaDB** database is used to store user registrations, user friend-list, pending friend requests, results of all games played as well as turns made in those games.
- the database structure is defined in `./server/src/database/Schema.kt`

#### In-memory database
- **redis** database is used to store the list of currently logged-in users
- this gives the possibility to horizontally scale the app in the future
    - the main reason was to learn/try-out **redis** database
  
### Client
#### Web server
- [nginx](https://www.nginx.com/) used

####  Application
- SPA (single page application)
- [yarn](https://yarnpkg.com/) as the package/project manager
- written purely in Javascript
- uses [Nuxt.js](https://nuxtjs.org/) frontend framework, that wraps and simplifies [Vue.js](https://vuejs.org/) usage
- [Vuetify](https://vuetifyjs.com) library used to create the UI (easy to use, implements Material Design)
- [Vuex](https://vuex.vuejs.org/) as the state management framework to separate logic into modules
    
## Deployment notes
In order to **try out the whole application** (while evaluating the semester project, including both server and client) run `docker-compose up` in the root of this repository.
  - after successful deployment (takes a while) the frontend is running on `http://localhost:80` and the server is running on `http://localhost:8080`
  - **super-admin account is available** after logging in with following credentials:
    - **email:** `admin@ttt-game.com`
    - **password:** `admin`
  - super-admin account (and other promoted admins ofcourse) is able to promote other admins to users (and also demote them)
    - super-admin account cannot be demoted
    
## Development notes
  - execute `docker-compose up` in the `./server` folder in order to run the server without the client
  - execute `yarn dev` in the `./client` folder in order to deploy only the client (hot reload supported)
