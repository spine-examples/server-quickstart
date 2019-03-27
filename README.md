# Spine-powered Server: Quickstart
A simple example of server application in Spine to get you started.

## Prerequisites

* Install JDK 8 or higher.
* Clone the source code: 
```bash
git clone git@github.com:SpineEventEngine/server-quickstart.git
```
* Run `./gradlew clean build` (or `gradlew.bat clean build` on Windows) in the project root folder.

## Structure

The project consists of three modules.

### The `model` Module

Defines the [ubiquitous language](https://martinfowler.com/bliki/UbiquitousLanguage.html) 
of the application in Protobuf.
The `model/src/main/proto` directory contains the Protobuf definitions of the domain model:
 * `Task` is an aggregate state type; as any entity type, it is marked with the `(entity)` option;
 * `TaskCreated` in `events.proto` is an event of the `Task` aggregate;
 * `CreateTask` in `commands.proto` is a command handled by the `Task` aggregate;
 * the model may also contain other message types, e.g. identifiers (see `identifiers.proto`), value
 types, etc.

### The `server` Module

1. Describes the business rules for Spine entities, such as Aggregates, in Java.
`TaskAggregate` class implements the `Task` aggregate by handling the `CreateTask` command and 
applying the produced `TaskCreated` event.

2. Plugs the `model` into the infrastructure: 
   * configures the storage;
   * creates a `BoundedContext` and registers repositories;
   * exposes the `BoundedContext` instance to the outer world through a set of gRPC services,
   provided by the framework.

See `io.spine.quickstart.server.ServerApp` for implementation. 

Run `ServerApp.main()` to start the server.

### The `client` Module

Interacts with the gRPC services, exposed by the `server` module: 
 * sends commands via `CommandService` stub;
 * sends queries via `QueryService` stub.

See `io.spine.quickstart.client.ClientApp` for implementation.

Run `ClientApp.main()` to start the client and see it connecting to the server.
 
## What's Next

This example is built on top of in-memory data storage, uses `io.spine.` package 
and demonstrates a really simple RPC interaction. To take it closer to the production needs,
the following steps are suggested:

### Gradle Configuration Changes:

 * Update `*.gradle` files with the artifact attributes, that correspond to your project.

### Suggested `model` Changes

 * Define the domain, in which the business task is solved.
 * Together with the domain experts, perform event storming in the bounded context of the domain
 speaking in language, ubiquitous for this domain.
 * According to the event storming results, define events, commands, entity states and value
objects in Protobuf. Put them into the `model/proto` folder. 
 * Design and implement Aggregates, Projections and Process Managers on top of the previously
 defined language elements. Create repositories for them.

### Suggested `server` Changes

 * Configure and the storage factory, that corresponds to the target environment. Connectors to
 [Google Datastore](https://github.com/SpineEventEngine/gcloud-java) and 
 [JDBC-enabled storages](https://github.com/SpineEventEngine/jdbc-storage) are provided by Spine.
 * Append the creation of the bounded context with the registration of newly created repositories.

Other possible changes include dealing with security (e.g. authentication/authorization), defining
deployment scheme, scaling approach etc. These and other advanced topics aren't covered by this
sample.

Typically these steps are repeated for each bounded context in the application.

For more information please visit [the official website](https://spine.io).
