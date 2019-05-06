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

import * as uuid from 'uuid';
import {init} from 'spine-web';
import {Type} from 'spine-web/client/typed-message';
import {ActorProvider} from 'spine-web/client/actor-request-factory';
import {CreateTask} from "../../generated/main/js/spine/quickstart/commands_pb"
import {Task} from "../../generated/main/js/spine/quickstart/task_pb"
import {TaskId} from "../../generated/main/js/spine/quickstart/identifiers_pb"

import * as spineTypes from 'spine-web/proto/index';
import * as types from '../../generated/main/js/index';

import * as firebase from 'firebase/app';
import 'firebase/database';

const HOST = 'http://localhost:8080';
const FIREBASE = firebase.initializeApp({
    databaseURL: 'ws://localhost:5000/',
    authDomain: 'ws://localhost:5000/'
});

class TaskController {

    constructor() {
        this._actorProvider = new ActorProvider();
        this._client = init({
            protoIndexFiles: [types, spineTypes],
            endpointUrl: HOST,
            firebaseDatabase: FIREBASE.database(),
            actorProvider: this._actorProvider
        });
    }

    createTask(title) {
        const id = new TaskId();
        id.setValue(uuid.v4());
        const cmd = new CreateTask();
        cmd.setId(id);
        cmd.setTitle(title);
        this._client.sendCommand(cmd,
                                 () => console.log("Command sent."),
                                 (err) => console.log("Command errored: " + err),
                                 (rej) => console.log("Command rejected: " + rej));
    }

    renderTasksIn(viewContainer) {
        const targetType = Type.forClass(Task);
        console.log("Subscribing to updates of " + targetType.url().value());
        this._client.subscribeToEntities({
            ofType: targetType
        }).then(({itemAdded, itemChanged, itemRemoved, unsubscribe}) => {
            itemAdded.subscribe(
                item => TaskController._renderNewTask(viewContainer, item)
            );
        });
    }

    static _renderNewTask(viewContainer, task) {
        viewContainer.innerHTML += TaskController._render(task);
    }

    static _render(task) {
        return "<a id='" + task.getId().getValue() + "'><div class='task'>" +
            task.getTitle() + "</div></a>";
    }
}

const controller = new TaskController();

function sendCommand(document) {
    const titleTextArea = document.getElementById('title-text');
    const title = titleTextArea.value;
    if (title.length > 0) {
        controller.createTask(title);
        titleTextArea.value = "";
    }
}

function subscribeToUpdates(document) {
    const container = document.getElementById('task-container');
    controller.renderTasksIn(container);
}

export {sendCommand, subscribeToUpdates};
