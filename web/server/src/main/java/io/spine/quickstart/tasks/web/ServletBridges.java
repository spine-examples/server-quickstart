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

package io.spine.quickstart.tasks.web;

import io.spine.tasks.server.TasksContext;
import io.spine.web.firebase.query.FirebaseQueryBridge;
import io.spine.web.firebase.subscription.FirebaseSubscriptionBridge;
import io.spine.web.query.QueryBridge;
import io.spine.web.subscription.SubscriptionBridge;

/**
 * A factory of the bridges between the servlet API and the {@code Tasks} context.
 */
final class ServletBridges {

    /**
     * Prevents the utility class instantiation.
     */
    private ServletBridges() {
    }

    static SubscriptionBridge subscription() {
        return FirebaseSubscriptionBridge
                .newBuilder()
                .setFirebaseClient(Firebase.client())
                .setSubscriptionService(TasksContext.subscriptionService())
                .build();
    }

    static QueryBridge query() {
        return FirebaseQueryBridge
                .newBuilder()
                .setFirebaseClient(Firebase.client())
                .setQueryService(TasksContext.queryService())
                .build();
    }
}
