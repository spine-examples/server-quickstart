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

package io.spine.tasks.server.task;

import io.spine.server.BoundedContextBuilder;
import io.spine.tasks.Task;
import io.spine.tasks.TaskId;
import io.spine.tasks.command.CreateTask;
import io.spine.tasks.event.TaskCreated;
import io.spine.testing.server.blackbox.BlackBoxContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("`TaskAggregate` should")
class TaskAggregateTest {

    @Test
    @DisplayName("emit `TaskCreated` event on `CreateTask` command and change state")
    void handleCreateTask() {
        TaskId taskId = TaskId.generate();
        String taskTitle = "Learn Domain-driven Design.";
        CreateTask command = CreateTask
                .newBuilder()
                .setId(taskId)
                .setTitle(taskTitle)
                .vBuild();
        Task expectedState = Task
                .newBuilder()
                .setId(taskId)
                .setTitle(taskTitle)
                .vBuild();
        BoundedContextBuilder contextBuilder = BoundedContextBuilder
                .assumingTests()
                .add(TaskAggregate.class);
        BlackBoxContext context = BlackBoxContext
                .from(contextBuilder)
                .receivesCommand(command);
        context.assertEvents()
               .withType(TaskCreated.class)
               .hasSize(1);
        context.assertEntityWithState(taskId, Task.class)
               .hasStateThat()
               .isEqualTo(expectedState);
    }
}
