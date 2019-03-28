/*
 * Copyright 2019, TeamDev. All rights reserved.
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
 */
package io.spine.quickstart.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Query;
import io.spine.client.QueryResponse;
import io.spine.client.grpc.CommandServiceGrpc;
import io.spine.client.grpc.QueryServiceGrpc;
import io.spine.core.Ack;
import io.spine.core.Command;
import io.spine.core.UserId;
import io.spine.core.UserIdVBuilder;
import io.spine.logging.Logging;
import io.spine.quickstart.CreateTask;
import io.spine.quickstart.CreateTaskVBuilder;
import io.spine.quickstart.Task;
import io.spine.serverapp.TaskId;
import io.spine.serverapp.TaskIdVBuilder;
import io.spine.string.Stringifiers;
import org.slf4j.Logger;

import java.text.ParseException;

import static io.spine.base.Identifier.newUuid;

/**
 * A template of a client for Spine-powered server.
 *
 * <p>Illustrates a simple flow:
 *
 * <ul>
 *     <li>establishes a connection to the gRPC server;
 *     <li>sends a command to create a task through {@code CommandService};
 *     <li>verifies that the task is created by asking for all tasks via {@code QueryService}.
 * </ul>
 */
public class ClientApp {

    /**
     * A host of the gRPC server to connect.
     */
    private static final String HOST = "127.0.0.1";

    /**
     * A port at which gRPC server listens for connections.
     */
    private static final int PORT = 8484;

    /**
     * Prevents this class from instantiation.
     */
    private ClientApp() {
    }

    /**
     * Connects to the gRPC server and interacts with it via exposed gRPC services.
     *
     * <p>Uses the hard-coded {@linkplain #HOST host} and {@linkplain #PORT port} for simplicity.
     */
    public static void main(String[] args) throws InterruptedException {

        // Connect to the server and init the client-side stubs for gRPC services.
        log().info("Connecting to the server at {}:{}", HOST, PORT);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                                                      .usePlaintext()
                                                      .build();
        CommandServiceGrpc.CommandServiceBlockingStub commandService =
                CommandServiceGrpc.newBlockingStub(channel);

        QueryServiceGrpc.QueryServiceBlockingStub queryService =
                QueryServiceGrpc.newBlockingStub(channel);

        // Create and post a command.
        ActorRequestFactory requestFactory = ActorRequestFactory
                .newBuilder()
                .setActor(whoIsCalling())
                .build();
        TaskId taskId = TaskIdVBuilder
                .newBuilder()
                .setValue(newUuid())
                .build();
        CreateTask createTask = newCreateTaskCommand(taskId, "Wash my car");
        Command cmd = requestFactory.command()
                                    .create(createTask);
        Ack acked = commandService.post(cmd);
        log().info("A command has been posted: " + Stringifiers.toString(createTask));
        log().info("(command acknowledgement: {})", Stringifiers.toString(acked));

        /*
         * Events, reflecting the changes caused by a command, travel from the write-side
         * to the read-side asynchronously.
         * Therefore some time should pass for the read-side to reflect the changes made.
         */
        Thread.sleep(100);

        // Create and post a query.
        Query readAllTasks = requestFactory.query()
                                           .all(Task.class);

        log().info("Reading all tasks...");
        QueryResponse response = queryService.read(readAllTasks);
        log().info("A response received: {}", Stringifiers.toString(response));
    }

    /**
     * Creates a message for the {@link CreateTask} command.
     *
     * @param taskId
     *         the ID of the new task
     * @param title
     *         the value to use for a title in the new task
     * @return the message for {@code CreateTask} command
     */
    private static CreateTask newCreateTaskCommand(TaskId taskId, String title) {
        CreateTask message = CreateTaskVBuilder
                .newBuilder()
                .setId(taskId)
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
        UserId actorId = UserIdVBuilder
                .newBuilder()
                .setValue(newUuid())
                .build();
        return actorId;
    }

    private static Logger log() {
        return Logging.get(ClientApp.class);
    }
}
