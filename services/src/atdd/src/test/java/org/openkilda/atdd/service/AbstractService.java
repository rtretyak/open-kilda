/* Copyright 2018 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.atdd.service;

import org.openkilda.atdd.utils.TimeFrame;

import lombok.AccessLevel;
import lombok.Getter;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public class AbstractService {
    @Getter(AccessLevel.PROTECTED)
    private final Client restClient;

    public AbstractService() {
        restClient = ClientBuilder.newClient(new ClientConfig());
    }

    protected Response doRequest(Invocation request) {
        Response response;
        TimeFrame timeFrame = new TimeFrame();
        try {
            response = request.invoke();
        } finally {
            timeFrame.close();
        }

        System.out.println(timeFrame.format("===> Request = duration %f sec"));
        System.out.println(String.format("===> Response = %d : %s", response.getStatus(), response.toString()));

        return response;
    }
}
