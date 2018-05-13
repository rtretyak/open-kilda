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

package org.openkilda.messaging.info.event;

import org.openkilda.messaging.StringSerializer;
import org.openkilda.messaging.Utils;
import org.openkilda.messaging.model.BidirectionalFlow;
import org.openkilda.messaging.model.Flow;
import org.openkilda.messaging.payload.flow.FlowEndpointPayload;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FlowSyncTest implements StringSerializer {
    @Test
    public void serializeLoop() throws Exception {
        final String flowId = getClass().getCanonicalName();
        final FlowEndpointPayload endAlpha = new FlowEndpointPayload("ff:fe:00:00:00:00:00:01", 9, 64);
        final FlowEndpointPayload endBeta = new FlowEndpointPayload("ff:fe:00:00:00:00:00:02", 9, 64);

        Flow halfFlow = new Flow(
                flowId, 1000, false, flowId + " description",
                endAlpha.getSwitchDpId(), endAlpha.getPortId(), endAlpha.getVlanId(),
                endBeta.getSwitchDpId(), endBeta.getPortId(), endBeta.getVlanId());
        BidirectionalFlow flow = new BidirectionalFlow(halfFlow, Flow.ofPeer(halfFlow, 0));
        FlowSync origin = new FlowSync(FlowSyncCommand.CREATE, flow);

        serialize(origin);
        FlowSync decoded = (FlowSync) deserialize();

        Assert.assertEquals(origin, decoded);
    }

    @Override
    public Object deserialize() throws IOException {
        return Utils.MAPPER.readValue(strings.poll(), FlowSync.class);
    }
}
