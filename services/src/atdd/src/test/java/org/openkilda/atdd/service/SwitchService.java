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

import org.openkilda.DefaultParameters;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;

public class SwitchService extends AbstractService {
    /**
     * portUp.
     */
    public boolean portUp(String switchName, int portNo) {
        System.out.println("\n==> Set Port Up");

        Invocation request = getRestClient()
                .target(DefaultParameters.trafficEndpoint)
                .path("/port/up")
                .queryParam("switch", switchName)
                .queryParam("port", portNo)
                .request()
                .buildPost(Entity.json(""));

        return doRequest(request).getStatus() == 200;
    }

    /**
     * portDown.
     */
    public boolean portDown(String switchName, int portNo) {
        System.out.println("\n==> Set Port Down");

        Invocation request = getRestClient()
                .target(DefaultParameters.trafficEndpoint)
                .path("/port/down")
                .queryParam("switch", switchName)
                .queryParam("port", portNo)
                .request()
                .buildPost(Entity.json(""));

        return doRequest(request).getStatus() == 200;
    }
}
