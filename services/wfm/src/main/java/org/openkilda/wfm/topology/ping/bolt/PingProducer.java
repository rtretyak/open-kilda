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

package org.openkilda.wfm.topology.ping.bolt;

import org.openkilda.messaging.model.BidirectionalFlow;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.error.AbstractException;
import org.openkilda.wfm.error.PipelineException;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

public class PingProducer extends AbstractBolt {
    public static final String BOLT_ID = ComponentId.PING_PRODUCER.toString();

    public static final String FIELD_ID_PING_ID = "ping-id";
    public static final String FIELD_ID_FLOW = "flow";
    public static final String FIELD_ID_DIRECTION = "direction";
    public static final String FIELD_ID_PING = "ping";

    public static final Fields STREAM_FIELDS = new Fields(
            FIELD_ID_PING_ID, FIELD_ID_FLOW, FIELD_ID_DIRECTION, FIELD_ID_PING, FIELD_ID_CONTEXT);

    @Override
    protected void handleInput(Tuple input) throws AbstractException {
        BidirectionalFlow flow;

        try {
            flow = (BidirectionalFlow) input.getValueByField(FlowFetcher.FIELD_ID_FLOW);
            // TODO
        } catch (ClassCastException e) {
            throw new PipelineException(this, input, FlowFetcher.FIELD_ID_FLOW, e.toString());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declare(STREAM_FIELDS);
    }
}
