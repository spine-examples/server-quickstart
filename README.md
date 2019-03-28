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
 * `TaskCreated` in `events.proto` is an event of the `TaskAggregate`;
 * `CreateTask` in `commands.proto` is a command handled by the `TaskAggregate`;
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

 * Experiment with the model. Create a new command type in `commands.proto`
```proto
message AssignDueDate {
    TaskId task_id = 1;
    spine.time.LocalDate due_date = 2 [(valid) = true, (required) = true, (when).in = FUTURE];
}
```
Remember to import `LocalDate` via `import "spine/time/time.proto";`. This type is provided by
the [Spine Time](https://github.com/SpineEventEngine/time) library. You don't have to perform any
additional steps to use it in your domain.
 * Create a new event type in `events.proto`:
```proto
message DueDateAssigned {
    TaskId task_id = 1;
    spine.time.LocalDate due_date = 2 [(valid) = true, (required) = true];
}
```
 * Adjust the aggregate state:
```proto
message Task {
    option (entity).kind = AGGREGATE;

    // An ID of the task.
    TaskId id = 1;

    // A title of the task.
    string title = 2 [(required) = true];

    // The date and time by which this task should be completed.
    spine.time.LocalDate due_date = 3 [(valid) = true, (required) = false];
}
```
Make sure to run a Gradle build after the changing the Protobuf definitions: 
```bash
./gradlew clean build
```` 
or for Windows:
```
gradlew.bat clean build
```
 * Handle the `AssignDueDate` command in the `TaskAggregate`:
```java
@Assign
DueDateAssigned handle(AssignDueDate command) {
    return DueDateAssignedVBuilder
            .newBuilder()
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
 * In `ClientApp`, extend the `main()` method. Post another command:
```java
AssignDueDate dueDateCommand = AssignDueDateVBuilder
        .newBuilder()
        .setTaskId(taskId)
        .setDueDate(LocalDates.of(2038, JANUARY, 19))
        .build();
commandService.post(requestFactory.command()
                                  .create(dueDateCommand));
```
and log the updated state:
```java
QueryResponse updatedStateResponse = queryService.read(taskQuery);
log().info("The second response received: {}", Stringifiers.toString(updatedStateResponse));
```
 * Restart the server. Run the client and make sure that the due date is set to the task. 

## Further Reading
 * [Core Spine concepts](https://spine.io/docs/guides/concepts.html)
 * [Domain model definition](https://spine.io/docs/guides/model-definition.html)
 * [Java Web server implementation](https://github.com/SpineEventEngine/web)
 * [JavaScript client library](https://www.npmjs.com/package/spine-web)
 * [An example project with a bigger model](https://github.com/SpineEventEngine/todo-list)
