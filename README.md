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

Defines the [Ubiquitous Language](https://martinfowler.com/bliki/UbiquitousLanguage.html) 
of the application in Protobuf.
The `model/src/main/proto` directory contains the Protobuf definitions of the domain model:
 * `Task` is an aggregate state type; as any entity type, it is marked with the `(entity)` option;
 * `TaskCreated` in `events.proto` is an event of the `Task` aggregate;
 * `CreateTask` in `commands.proto` is a command handled by the `Task` aggregate;
 * the model may also contain other message types, e.g. identifiers (see `identifiers.proto`), value
 types, etc.

### The `server` Module

1. Describes the business rules for Spine entities, such as Aggregates, in Java.
See the `TaskAggregate` which handles the `CreateTask` command and applies the produced
`TaskCreated` event.

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

 * Experiment with the model—create a new command type:
```proto
message AssignDueDate {
    
    TaskId task_id = 1;
    
    google.protobuf.Timestamp due_date = 2 [(when).in = FUTURE];
}
```
and a new event type:
```proto
message DueDateAssigned {
    
    TaskId task_id = 1;
    
    google.protobuf.Timestamp due_date = 2;   
}
```
 * Adjust the aggregate state:
```proto
// Defines a state for `Task` aggregate.
//
message Task {
    option (entity).kind = AGGREGATE;

    // An ID of the task.
    TaskId id = 1;


    // A title of the task.
    string title = 2 [(required) = true];
    
    // The date and time by which this task should be completed.
    google.protobuf.Timestamp due_date = 3; // New field
}
```
Make sure to run a Gradle build (e.g. `./gradlew clean assemble`) after the changes to the Protobuf
definitions.
 * Handle the `AssignDueDate` command in the `TaskAggregate`:
```java
@Assign
DueDateAssigned handle(AssignDueDate command) {
    return DueDateAssigned.vBuilder()
                          .setTaskId(command.getTaskId())
                          .setDueDate(command.getDueDate())
                          .build();
}
```
 * Apply the emitted event:
```java
@Apply
private void on(DueDateAssigned event) {
    builder().setDueDate(event.getDueDate());
}
```
 * In `ClientApp`, append the `main` method with another command posting:
```java
// -- ClientApp.main -- 
// ...

AssignDueDate dueDateCommand = AssignDueDate
    .vBuilder()
    .setTaskId(taskId)
    .setDueDate(Timestamps.parse("2038-01-19T03:14:07+00:00"))
    .build();
commandService.post(requestFactory.command()
                                  .create(dueDateCommand));
```
and check the updated state:
```java
QueryResponse updatedStateResponse = queryService.read(readAllTasks);
log().info("The second response received: {}", Stringifiers.toString(response));
```
 * Restart the server. Run the client and make sure that the due date is set to the task. 

### Further Reading
 * [Core Spine concepts](https://spine.io/docs/guides/concepts.html)
 * [Domain model definition](https://spine.io/docs/guides/model-definition.html)
 * [Java Web server implementation](https://github.com/SpineEventEngine/web)
 * [JavaScript client library](https://www.npmjs.com/package/spine-web)
 * [An example project with a bigger model](https://github.com/SpineEventEngine/todo-list)
