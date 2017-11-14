/*
 *
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package io.spine.example.quickstart;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.spine.Identifier;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Query;
import io.spine.client.QueryResponse;
import io.spine.client.grpc.CommandServiceGrpc;
import io.spine.client.grpc.CommandServiceGrpc.CommandServiceBlockingStub;
import io.spine.client.grpc.QueryServiceGrpc;
import io.spine.client.grpc.QueryServiceGrpc.QueryServiceBlockingStub;
import io.spine.core.Ack;
import io.spine.core.BoundedContextName;
import io.spine.core.Command;
import io.spine.core.UserId;
import io.spine.example.quickstart.c.CreateTask;
import io.spine.example.serverapp.TaskId;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.GrpcContainer;
import io.spine.string.Stringifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.spine.server.BoundedContext.newName;

/**
 * A template of a server application, powered by Spine.
 *
 * <p>Creates an instance of a sample bounded context and exposes it via gRPC services.
 *
 * @author Alex Tymchenko
 */
public class ServerApp {

    /**
     * A name of the bounded context.
     */
    private static final BoundedContextName BOUNDED_CONTEXT_NAME = newName("Tasks");

    /**
     * A flag determining whether the server application allows multiple tenants.
     */
    private static final boolean MULTITENANT = false;

    /**
     * A host to use for gRPC server.
     */
    private static final String HOST = "127.0.0.1";

    /**
     * A port to use for gRPC server.
     */
    private static final int PORT = 8484;

    /**
     * This class must not be instantiated, as it's just a holder for {@code main} method.
     */
    private ServerApp() {
    }

    public static void main(String[] args) throws IOException {
        // Start a gRPC server, exposing the `Tasks` bounded context.
        startServer();

        // and then connect to it with a simple gRPC client application.
        runClient();
    }

    /**
     * Creates and starts a gRPC server and serves `Tasks` bounded context.
     *
     * <p>Uses the hard-coded {@linkplain #HOST host} and {@linkplain #PORT port} for simplicity.
     *
     * @throws IOException if gRPC server cannot be started
     */
    private static void startServer() throws IOException {

        // Define an in-memory storage factory, which allows the only tenant.

        final StorageFactory storageFactory =
                InMemoryStorageFactory.newInstance(BOUNDED_CONTEXT_NAME, MULTITENANT);

        final BoundedContext boundedContext =
                BoundedContext.newBuilder()
                              .setStorageFactorySupplier(() -> storageFactory)
                              .build();
        final TaskRepository repository = new TaskRepository();
        boundedContext.register(repository);

        /*
         * Instantiate gRPC services provided by Spine
         * and configure them for the given {@code BoundedContext}.
         */
        final CommandService commandService = CommandService.newBuilder()
                                                            .add(boundedContext)
                                                            .build();
        final QueryService queryService = QueryService.newBuilder()
                                                      .add(boundedContext)
                                                      .build();

        /*
         * Deploy the services to the gRPC container.
         */
        final GrpcContainer container = GrpcContainer.newBuilder()
                                                     .setPort(PORT)
                                                     .addService(commandService)
                                                     .addService(queryService)
                                                     .build();
        container.start();
    }

    /**
     * Creates a client for previously started gRPC server and sends a few demo requests.
     *
     * <p>Uses the hard-coded {@linkplain #HOST host} and {@linkplain #PORT port} for simplicity.
     */
    private static void runClient() {

        // Connect to the server and init the client-side stubs for gRPC services.
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                                                            .usePlaintext(true)
                                                            .build();
        final CommandServiceBlockingStub clientCommandService =
                CommandServiceGrpc.newBlockingStub(channel);

        final QueryServiceBlockingStub queryClientService =
                QueryServiceGrpc.newBlockingStub(channel);

        // Create and post a command.
        final ActorRequestFactory requestFactory = ActorRequestFactory.newBuilder()
                                                                      .setActor(whoIsCalling())
                                                                      .build();
        final CreateTask createTask = newCreateTaskMsg("Wash my car");
        final Command cmd = requestFactory.command()
                                          .create(createTask);

        final Ack acked = clientCommandService.post(cmd);
        log().info("A command has been posted: " + Stringifiers.toString(createTask));
        log().info("(command acknowledgement: {})", Stringifiers.toString(acked));

        // Create and post a query.
        final Query readAllTasks = requestFactory.query()
                                                 .all(Task.class);

        log().info("Reading all tasks...");
        final QueryResponse response = queryClientService.read(readAllTasks);
        log().info("A response received: {}", Stringifiers.toString(response));
    }

    private static CreateTask newCreateTaskMsg(String title) {
        final TaskId newTaskId = TaskId.newBuilder()
                                       .setValue(Identifier.newUuid())
                                       .build();
        final CreateTask message = CreateTask.newBuilder()
                                             .setId(newTaskId)
                                             .setTitle(title)
                                             .build();
        return message;
    }

    /**
     * Obtains an ID of the user, who posts commands.
     *
     * <p>Generated randomly each time for simplicity.
     *
     * <p>Must be substituted with a real {@code UserId} in a production application.
     */
    private static UserId whoIsCalling() {
        final UserId actorId = UserId.newBuilder()
                                     .setValue(Identifier.newUuid())
                                     .build();
        return actorId;
    }

    /**
     * A singleton logger to use in scope of this application.
     */
    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ServerApp.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
