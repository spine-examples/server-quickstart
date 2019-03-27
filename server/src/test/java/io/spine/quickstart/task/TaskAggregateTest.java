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

package io.spine.quickstart.task;

import io.spine.quickstart.CreateTask;
import io.spine.quickstart.CreateTaskVBuilder;
import io.spine.quickstart.Task;
import io.spine.quickstart.TaskCreated;
import io.spine.server.DefaultRepository;
import io.spine.serverapp.TaskId;
import io.spine.testing.server.blackbox.BlackBoxBoundedContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.testing.server.blackbox.verify.state.VerifyState.exactlyOne;

@DisplayName("TaskAggregate should")
class TaskAggregateTest {

    @Test
    @DisplayName("emmit TaskCreated event on CreateTask command")
    void handleCreateTask() {
        TaskId taskId = TaskId
                .newBuilder()
                .setValue(newUuid())
                .build();
        String taskTitle = "Learn Domain Driven Design.";
        CreateTask command = CreateTaskVBuilder
                .newBuilder()
                .setId(taskId)
                .setTitle(taskTitle)
                .build();
        Task expectedState = Task
                .newBuilder()
                .setId(taskId)
                .setTitle(taskTitle)
                .build();
        BlackBoxBoundedContext
                .singleTenant()
                .with(DefaultRepository.of(TaskAggregate.class))
                .receivesCommand(command)
                .assertEmitted(TaskCreated.class)
                .assertThat(exactlyOne(expectedState));
    }
}
