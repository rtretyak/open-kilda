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

package org.openkilda.wfm.topology.flow.bolts.sync;

import org.openkilda.messaging.Utils;
import org.openkilda.messaging.info.InfoMessage;
import org.openkilda.messaging.info.flow.FlowInfoData;
import org.openkilda.messaging.info.flow.FlowOperation;
import org.openkilda.wfm.CommandContext;
import org.openkilda.wfm.topology.flow.StreamType;
import org.openkilda.wfm.topology.flow.bolts.CrudBolt;

import org.apache.storm.tuple.MessageId;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FlowSyncRouterTest extends AbstractFlowSyncTest {
    private FlowSyncRouter subject;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        subject = new FlowSyncRouter();
        initBolt(subject);
    }

    @Test
    public void positive() throws Exception {
        final Tuple input = makeInput(FlowOperation.CREATE);
        String json = input.getStringByField(CrudBolt.FIELD_ID_MESSAGE);
        final InfoMessage message = Utils.MAPPER.readValue(json, InfoMessage.class);
        final FlowInfoData payload = (FlowInfoData) message.getData();

        Values result = feedBolt(subject, input);
        Assert.assertEquals(FLOW_ID, result.get(0));
        Assert.assertEquals(message.getData(), result.get(1));
        Assert.assertTrue(result.get(2) instanceof CommandContext);

        CommandContext context = (CommandContext) result.get(2);
        Assert.assertEquals(payload.getCorrelationId(), context.getCorrelationId());
    }

    private Tuple makeInput(FlowOperation operation) throws Exception {
        FlowInfoData payload = makeFlowInfoRecord(operation);
        InfoMessage message = new InfoMessage(payload, System.currentTimeMillis(), payload.getCorrelationId());
        String json = Utils.MAPPER.writeValueAsString(message);

        MessageId anchorId = MessageId.makeUnanchored();
        return new TupleImpl(
                getTopologyContext(), new Values(json), TASK_ID_CRUD, StreamType.CREATE.toString(), anchorId);
    }
}
