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

package io.spine.quickstart;

import io.spine.quickstart.task.TaskAggregate;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.DefaultRepository;
import io.spine.server.QueryService;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;

import static io.spine.core.BoundedContextNames.newName;

public final class TasksContext {

    public static final String NAME = "Tasks";
    private static final boolean MULTITENANT = false;

    private static final BoundedContext context = createContext();

    private static final QueryService queryService = QueryService
            .newBuilder()
            .add(context)
            .build();
    private static final CommandService commandService = CommandService
            .newBuilder()
            .add(context)
            .build();

    /**
     * Prevents the utility class instantiation.
     */
    private TasksContext() {
    }

    public static BoundedContext instance() {
        return context;
    }

    private static BoundedContext createContext() {
        StorageFactory storage = InMemoryStorageFactory.newInstance(newName(NAME), MULTITENANT);
        BoundedContext context = BoundedContext
                .newBuilder()
                .setName(NAME)
                .setStorageFactorySupplier(() -> storage)
                .add(DefaultRepository.of(TaskAggregate.class))
                .build();
        return context;
    }

    public static QueryService queryService() {
        return queryService;
    }

    public static CommandService commandService() {
        return commandService;
    }
}
