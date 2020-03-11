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
package io.spine.tasks.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.spine.client.Client;
import io.spine.client.Subscription;
import io.spine.core.UserId;
import io.spine.tasks.Task;
import io.spine.tasks.TaskId;
import io.spine.tasks.command.CreateTask;
import io.spine.tasks.event.TaskCreated;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import static io.spine.base.Identifier.newUuid;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A template of a standalone Java client for Spine-powered server.
 *
 * <p>Illustrates a simple flow:
 *
 * <ul>
 *      <li>establishes a connection to the gRPC server;
 *      <li>sends a command to create a task through {@code CommandService};
 *      <li>verifies that the task is created by asking for all tasks via {@code QueryService}.
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

        // Connect to the server and init the client instance.
        info("Connecting to the server at %s:%d.", HOST, PORT);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                                                      .usePlaintext()
                                                      .build();
        Client client = Client.usingChannel(channel)
                              .build();
        UserId user = whoIsCalling();

        /*
         * Generate a new task ID.
         *
         * For the Protobuf messages consisting of a single field named {@code uuid}
         * the framework automatically creates the {@code generate()} method.
         *
         * This factory method uses a randomly generated UUID value to create a new instance
         * of the Protobuf message.
         */
        TaskId taskId = TaskId.generate();
        CreateTask createTask = newCreateTaskCommand(taskId, "Reset wall clock");

        /*
         * Send a command to the server and observe the events produced by this command.
         */
        CountDownLatch taskCreated = new CountDownLatch(1);
        ImmutableSet<Subscription> subscriptions =
                client.onBehalfOf(user)
                      .command(createTask)
                      .observe(TaskCreated.class, event -> taskCreated.countDown())
                      .onStreamingError(ClientApp::throwProcessingError)
                      .post();

        /*
         * Events, reflecting the changes caused by a command, travel from the write-side
         * to the read-side asynchronously.
         * Therefore some time should pass for the read-side to reflect the changes made.
         */
        taskCreated.await();

        // Cancel the event subscriptions.
        subscriptions.forEach(client::cancel);

        info("Reading the task...");
        ImmutableList<Task> tasks = client.onBehalfOf(user)
                                          .select(Task.class)
                                          .byId(taskId)
                                          .run();
        info("A response received: %s", tasks);
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

    private static void throwProcessingError(Throwable throwable) {
        throw newIllegalStateException(
                throwable, "An error while processing the command result."
        );
    }

    private static void info(String msg) {
        log.at(Level.INFO)
           .log(msg);
    }

    private static void info(String msg, Object arg1) {
        log.at(Level.INFO)
           .log(msg, arg1);
    }

    private static void info(String msg, Object arg1, Object arg2) {
        log.at(Level.INFO)
           .log(msg, arg1, arg2);
    }
}
