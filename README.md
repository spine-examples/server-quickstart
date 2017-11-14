# server-template
A quickstart template of a server-side application powered by Spine.

The template consists of two modules.

### `model` Module

Defines the [ubiquitous language](https://martinfowler.com/bliki/UbiquitousLanguage.html) of the application in Protobuf.

Describes the business rules for Spine entities, such as Aggregates, in Java.


### `server` Module

Plugs the `model` into the infrastructure: 
 * configures the storage;
 * creates a `BoundedContext` and registers repositories;
 * exposes the `BoundedContext` instance to the outer world through a set of gRPC services, provided by the framework.
