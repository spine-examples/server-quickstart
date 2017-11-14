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

import com.google.common.base.Optional;
import io.spine.Identifier;
import io.spine.client.ActorRequestFactory;
import io.spine.core.BoundedContextName;
import io.spine.core.Command;
import io.spine.core.UserId;
import io.spine.example.quickstart.c.CreateTask;
import io.spine.example.quickstart.c.TaskCreated;
import io.spine.example.serverapp.TaskId;
import io.spine.grpc.StreamObservers;
import io.spine.server.BoundedContext;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateRepository;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.string.Stringifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.spine.server.BoundedContext.newName;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A template of a server application, powered by Spine.
 *
 * <p>Creates an instance of bounded context and exposes it via gRPC services.
 *
 * @author Alex Tymchenko
 */
public class ServerApp {

    private static final BoundedContextName BOUNDED_CONTEXT_NAME = newName("Tasks");
    private static final boolean MULTITENANT = false;

    /**
     * This class must not be instantiated, as it's just a holder for {@code main} method.
     */
    private ServerApp() {}

    public static void main(String[] args) {
        final StorageFactory storageFactory =
                InMemoryStorageFactory.newInstance(BOUNDED_CONTEXT_NAME, MULTITENANT);

        final BoundedContext boundedContext =
                BoundedContext.newBuilder()
                              .setStorageFactorySupplier(() -> storageFactory)
                              .build();
        final TaskRepository repository = new TaskRepository();
        boundedContext.register(repository);


        // Create and post a command.

        final ActorRequestFactory requestFactory = ActorRequestFactory.newBuilder()
                                                                      .setActor(whoIsCalling())
                                                                      .build();
        final CreateTask createTask = newCreateTaskMsg("Wash my car");
        final Command cmd = requestFactory.command()
                                          .create(createTask);
        boundedContext.getCommandBus()
                      .post(cmd, StreamObservers.noOpObserver());
        log().info("A command has been posted: " + Stringifiers.toString(createTask));


        // Verify than the aggregate instance has been created.

        final Optional<TaskAggregate> aggregate = repository.find(createTask.getId());

        if (!aggregate.isPresent()) {
            throw newIllegalStateException("The aggregate instance with ID {}" +
                                                   " must have been created.", createTask.getId());
        }

        final TaskAggregate taskAggregate = aggregate.get();
        log().info("An aggregate has been created: " +
                                   Stringifiers.toString(taskAggregate.getState()));
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
     * Creates a random ID of user, who posts commands.
     *
     * <p>This information is required by the framework.
     */
    private static UserId whoIsCalling() {
        final UserId actorId = UserId.newBuilder()
                                     .setValue(Identifier.newUuid())
                                     .build();
        return actorId;
    }

    /**
     * Definition of the {@code Task} aggregate.
     *
     * <p>Within this small example it only handles a single command and emits one event.
     */
    static final class TaskAggregate extends Aggregate<TaskId, Task, TaskVBuilder> {

        TaskAggregate(TaskId id) {
            super(id);
        }

        @SuppressWarnings("unused")     // The method is called by Spine via reflection.
        @Assign
        public TaskCreated handle(CreateTask cmd) {
            final TaskCreated result = TaskCreated.newBuilder()
                                                  .setTitle(cmd.getTitle())
                                                  .setId(cmd.getId())
                                                  .build();
            return result;
        }

        @SuppressWarnings("unused")     // The method is called by Spine via reflection.
        @Apply
        public void on(TaskCreated event) {
            getBuilder().setTitle(event.getTitle());
        }
    }

    /**
     * A repository of {@link TaskAggregate} instances
     */
    private static final class TaskRepository extends AggregateRepository<TaskId, TaskAggregate> {}

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ServerApp.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
