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

import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
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
import io.spine.quickstart.tasks.command.CreateTask;
import io.spine.quickstart.tasks.task.Task;
import io.spine.quickstart.tasks.TaskId;
import io.spine.string.Stringifiers;

import java.util.logging.Level;

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

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

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
        info("Connecting to the server at {}:{}", HOST, PORT);

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
        TaskId taskId = TaskId
                .newBuilder()
                .setValue(newUuid())
                .vBuild();
        CreateTask createTask = newCreateTaskCommand(taskId, "Reset wall clock");
        Command cmd = requestFactory.command()
                                    .create(createTask);
        Ack acked = commandService.post(cmd);
        info("A command has been posted: " + Stringifiers.toString(createTask));
        info("(command acknowledgement: {})", Stringifiers.toString(acked));

        /*
         * Events, reflecting the changes caused by a command, travel from the write-side
         * to the read-side asynchronously.
         * Therefore some time should pass for the read-side to reflect the changes made.
         */
        Thread.sleep(100);

        // Create and execute a query.
        Query taskQuery = requestFactory.query()
                                        .byIds(Task.class, ImmutableSet.of(taskId));
        info("Reading the task...");
        QueryResponse response = queryService.read(taskQuery);
        info("A response received: {}", Stringifiers.toString(response));
    }

    private static void info(String msg, Object ...args) {
        log.at(Level.INFO).log(msg, args);
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
        CreateTask message = CreateTask
                .newBuilder()
                .setId(taskId)
                .setTitle(title)
                .vBuild();
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
        UserId actorId = UserId
                .newBuilder()
                .setValue(newUuid())
                .vBuild();
        return actorId;
    }
}
