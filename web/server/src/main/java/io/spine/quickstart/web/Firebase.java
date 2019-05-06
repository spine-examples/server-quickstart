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

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import io.spine.net.Url;
import io.spine.web.firebase.DatabaseUrl;
import io.spine.web.firebase.FirebaseClient;
import io.spine.web.firebase.rest.RestClient;

final class Firebase {

    private static final Url EMULATOR_URL = Url
            .newBuilder()
            .setSpec("http://127.0.0.1:5000/")
            .build();

    private static final FirebaseClient client = createClient();

    /**
     * Prevents the utility class instantiation.
     */
    private Firebase() {
    }

    static FirebaseClient client() {
        return client;
    }

    private static FirebaseClient createClient() {
        DatabaseUrl url = DatabaseUrl
                .newBuilder()
                .setUrl(EMULATOR_URL)
                .build();
        HttpTransport transport = new ApacheHttpTransport();
        HttpRequestFactory requestFactory = transport.createRequestFactory();
        return RestClient.create(url, requestFactory);
    }
}
