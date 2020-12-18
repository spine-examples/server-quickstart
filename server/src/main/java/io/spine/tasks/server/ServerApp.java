/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
package io.spine.tasks.server;

import com.google.common.flogger.FluentLogger;
import io.spine.server.CommandService;
import io.spine.server.GrpcContainer;
import io.spine.server.QueryService;
import io.spine.server.SubscriptionService;

import java.io.IOException;
import java.util.logging.Level;

/**
 * A template of a server application, powered by Spine.
 *
 * <p>Creates an instance of a sample bounded context and exposes it via gRPC services.
 *
 * <p>Also uses a simple gRPC client to connect to the server-side and illustrate the workflow.
 */
public final class ServerApp {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

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

    /**
     * Creates and starts a gRPC server and serves {@code Tasks} bounded context.
     *
     * <p>Uses the hard-coded {@linkplain #HOST host} and {@linkplain #PORT port} for simplicity.
     *
     * @throws IOException
     *         if the gRPC server cannot be started
     */
    public static void main(String[] args) throws IOException {
        CommandService commandService = TasksContext.commandService();
        QueryService queryService = TasksContext.queryService();
        SubscriptionService subscriptionService = TasksContext.subscriptionService();

        GrpcContainer container = GrpcContainer
                .atPort(PORT)
                .addService(commandService)
                .addService(queryService)
                .addService(subscriptionService)
                .build();
        container.start();
        log.at(Level.INFO).log("gRPC server started at %s:%d.", HOST, PORT);

        container.awaitTermination();
    }
}
