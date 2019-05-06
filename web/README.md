# Web Quickstart

This example shows the simplest way of serving a Spine application publicly via HTTP and connecting
to it via a web client.

### Server

The web server for the `Tasks` context is defined in the [`web/server`](./server) module.

A Spine web server consists of a number of servlets. Each servlet extends an abstract base defined
in the [`spine-web`](https://github.com/SpineEventEngine/web) library.

The query-side servlets require a way of delivering the query responses to the client. See 
the `firebase-web` library for the implementation based on the Firebase Realtime Database.

For the purposes of this example, the server starts a Firebase emulator, so that users don't have to
specify their own Firebase application credentials.

### Client

The web client for the `Tasks` context is defined in the [`web/client`](./client) module.

The simplistic JavaScript client consists of a single which both posts the command and performs 
queries.

Note that the client module is a Gradle project. Because of this, the client is capable of compiling
JS sources from the Protobuf definitions. See [the build script](./client/build.gradle) for 
the details.

## Running

In order to contemplate the web modules in action, follow these steps:
 1. In the `web/client`:
    - assemble the JavaScript via `npm install`;
    - build a single javascript artifact via `webpack`.
 2. In the root directory, launch the server with `./gradlew :web:runServer`.
 3. Open the [main page](./client/app/index.html) in the browser.
 
Note that the process started in the step 2 will end only when the server is shut down.

After the app is started, the page subscribes to the updates of `Task`s. Type a task title into 
the test input and press the `Create` button. The command is sent to the server. The server writes
the updated entity state to the Firebase and the client receives the update and displays the new
task on the screen.
