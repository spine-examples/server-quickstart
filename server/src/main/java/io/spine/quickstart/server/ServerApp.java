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
package io.spine.quickstart.server;

import io.spine.logging.Logging;
import io.spine.quickstart.TasksContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.transport.GrpcContainer;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A template of a server application, powered by Spine.
 *
 * <p>Creates an instance of a sample bounded context and exposes it via gRPC services.
 *
 * <p>Also uses a simple gRPC client to connect to the server-side and illustrate the workflow.
 */
public class ServerApp {

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

        GrpcContainer container = GrpcContainer
                .newBuilder()
                .setPort(PORT)
                .addService(commandService)
                .addService(queryService)
                .build();
        container.start();
        log().info("gRPC server started at {}:{}.", HOST, PORT);

        container.awaitTermination();
    }

    private static Logger log() {
        return Logging.get(ServerApp.class);
    }
}
