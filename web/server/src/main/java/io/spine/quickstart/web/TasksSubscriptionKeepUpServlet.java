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

package io.spine.quickstart.web;

import io.spine.web.subscription.servlet.SubscriptionKeepUpServlet;

import javax.servlet.annotation.WebServlet;

/**
 * {@code Tasks} context {@code /subscription/keep-up} servlet.
 *
 * <p>This is a part of the system's subscription web API. Handles the subscription keep-up
 * requests via the {@link io.spine.web.firebase.subscription.FirebaseSubscriptionBridge}.
 *
 * @see SubscriptionKeepUpServlet
 * @see io.spine.quickstart.web.ServletBridges
 */

@WebServlet("/subscription/keep-up")
public final class TasksSubscriptionKeepUpServlet extends SubscriptionKeepUpServlet {

    private static final long serialVersionUID = 0L;

    public TasksSubscriptionKeepUpServlet() {
        super(ServletBridges.subscription());
    }
}
