# server-template
A quickstart template of a server-side application powered by Spine.

## Structure

The template consists of two modules.

### `model` Module

Defines the [ubiquitous language](https://martinfowler.com/bliki/UbiquitousLanguage.html) 
of the application in Protobuf.

Describes the business rules for Spine entities, such as Aggregates, in Java.


### `server` Module

Plugs the `model` into the infrastructure: 
 * configures the storage;
 * creates a `BoundedContext` and registers repositories;
 * exposes the `BoundedContext` instance to the outer world through a set of gRPC services, 
 provided by the framework.
 
 
 
## What's Next

This example is built on top of in-memory data storage, uses `io.spine.` package 
and demonstrates a really simple RPC interaction. To take it closer to the production needs, 
the following steps are suggested:

### Suggested `model` Changes 

 * Define the domain, in which the business task is solved.
 * Together with the domain experts, perform event storming in the bounded context of the domain 
 speaking in language, ubiquitous for this domain.
 * According to the event storming results, define events, commands, entity states and value 
objects in Protobuf. Put them into the `model/proto` folder. 
 * Adjust the Protobuf package structure to your taste. However, keeping `foo.bar.c` (stands for
 "**C**ommand"  in CQRS) and `foo.bar.q` (stands for "**Q**uery" in CQRS) sub-packages is known 
 to be convenient for conceptual separation.
 * Design and implement Aggregates, Projections and Process Managers on top of the previously 
 defined language elements. Create repositories for them.

### Suggested `server` Changes 

 * Configure and the storage factory, that corresponds to the target environment. Connectors to 
 [Google Datastore](https://github.com/SpineEventEngine/gae-java) and [JDBC-enabled storages](https://github.com/SpineEventEngine/jdbc-storage)
 are provided by Spine.
 * Append the creation of the bounded context with the registration of newly created repositories.
 * Design and implement the security layer.
 * ...


Typically these steps are repeated for each bounded context in the application. 


 
