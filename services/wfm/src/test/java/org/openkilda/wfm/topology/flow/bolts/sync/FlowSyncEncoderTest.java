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
import org.openkilda.messaging.info.event.FlowSync;
import org.openkilda.messaging.info.event.FlowSyncCommand;
import org.openkilda.messaging.info.flow.FlowInfoData;
import org.openkilda.messaging.info.flow.FlowOperation;
import org.openkilda.messaging.model.BidirectionalFlow;
import org.openkilda.wfm.CommandContext;

import org.apache.storm.tuple.MessageId;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FlowSyncEncoderTest extends AbstractFlowSyncTest {
    private FlowSyncEncoder subject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        subject = new FlowSyncEncoder();
        initBolt(subject);
    }

    @Test
    public void name() throws Exception {
        Tuple input = makeInput(FlowOperation.CREATE);
        Values output = feedBolt(subject, input);
        Assert.assertEquals(3, output.size());

        FlowSync syncRecord = (FlowSync) input.getValueByField(FlowSyncAssembler.FIELD_ID_PAYLOAD);

        String flowId = (String) output.get(0);
        Assert.assertEquals(syncRecord.getFlow().getFlowId(), flowId);

        String json = (String) output.get(1);
        InfoMessage message = Utils.MAPPER.readValue(json, InfoMessage.class);
        FlowSync encodedSyncRecord = (FlowSync) message.getData();
        Assert.assertEquals(syncRecord, encodedSyncRecord);

        Assert.assertNull(output.get(2));
    }

    private Tuple makeInput(FlowOperation operation) {
        FlowInfoData payload = makeFlowInfoRecord(operation);
        BidirectionalFlow flow = new BidirectionalFlow(payload.getPayload());
        FlowSync sync = new FlowSync(FlowSyncCommand.CREATE, flow);

        CommandContext context = new CommandContext(payload.getCorrelationId());
        Values values = new Values(sync, context);

        MessageId anchorId = MessageId.makeUnanchored();
        return new TupleImpl(
                getTopologyContext(), values,
                TASK_ID_FLOW_SYNC_ASSWMBLER, org.apache.storm.utils.Utils.DEFAULT_STREAM_ID, anchorId);
    }
}
