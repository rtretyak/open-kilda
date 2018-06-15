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

import org.openkilda.messaging.Utils;
import org.openkilda.messaging.model.BidirectionalFlow;
import org.openkilda.pce.provider.PathComputer;
import org.openkilda.pce.provider.PathComputerAuth;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.CommandContext;
import org.openkilda.wfm.error.AbstractException;
import org.openkilda.wfm.error.PipelineException;
import org.openkilda.wfm.share.utils.PathComputerFlowFetcher;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class FlowFetcher extends AbstractBolt {
    public static final String BOLT_ID = ComponentId.FLOW_FETCHER.toString();

    public static final String FIELD_ID_FLOW_ID = Utils.FLOW_ID;
    public static final String FIELD_ID_FLOW = "flow";

    public static final Fields STREAM_FIELDS = new Fields(FIELD_ID_FLOW_ID, FIELD_ID_FLOW, FIELD_ID_CONTEXT);

    private final PathComputerAuth pathComputerAuth;
    private PathComputer pathComputer = null;

    public FlowFetcher(PathComputerAuth pathComputerAuth) {
        this.pathComputerAuth = pathComputerAuth;
    }

    @Override
    protected void handleInput(Tuple input) throws AbstractException {
        String componentId = input.getSourceComponent();

        if (MonotonicTick.BOLT_ID.equals(componentId)) {
            handlePing(input);
        } else {
            unhandledInput(input);
        }
    }

    private void handlePing(Tuple input) throws PipelineException {
        PathComputerFlowFetcher fetcher = new PathComputerFlowFetcher(pathComputer);

        final CommandContext context = getContext(input);
        final OutputCollector output = getOutput();
        for (BidirectionalFlow flow : fetcher.getFlows()) {
            Values payload = new Values(flow.getFlowId(), flow, context);
            output.emit(input, payload);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declare(STREAM_FIELDS);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        pathComputer = pathComputerAuth.getPathComputer();
    }
}
