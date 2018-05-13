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

import org.openkilda.messaging.info.flow.FlowInfoData;
import org.openkilda.messaging.info.flow.FlowOperation;
import org.openkilda.messaging.model.Flow;
import org.openkilda.messaging.model.ImmutablePair;
import org.openkilda.messaging.payload.flow.FlowEndpointPayload;
import org.openkilda.wfm.AbstractBoltTest;
import org.openkilda.wfm.CommandContext;
import org.openkilda.wfm.topology.flow.bolts.CrudBolt;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.utils.Utils;
import org.junit.Before;
import org.mockito.Mockito;


abstract class AbstractFlowSyncTest extends AbstractBoltTest {
    protected static final int TASK_ID_CRUD = 0;
    protected static final int TASK_ID_FLOW_SYNC_ASSWMBLER = 1;
    protected static final int TASK_ID_FLOW_SYNC_ROUTER = 2;

    protected static final String FLOW_ID = "sync-router-test";
    protected static final FlowEndpointPayload ENDPOINT_ALPHA
            = new FlowEndpointPayload("ff:fe:00:00:00:00:00:01", 9, 64);
    protected static final FlowEndpointPayload ENDPOINT_BETA
            = new FlowEndpointPayload("ff:fe:00:00:00:00:00:02", 9, 64);

    @Before
    public void setUp() throws Exception {
        TopologyContext topologyContext = getTopologyContext();

        Mockito.when(topologyContext.getComponentId(TASK_ID_CRUD)).thenReturn(CrudBolt.BOLT_ID);
        Mockito.when(topologyContext.getComponentOutputFields(CrudBolt.BOLT_ID, CrudBolt.STREAM_CREATE_ID))
                .thenReturn(CrudBolt.STREAM_CREATE_FIELDS);

        Mockito.when(topologyContext.getComponentId(TASK_ID_FLOW_SYNC_ASSWMBLER)).thenReturn(FlowSyncAssembler.BOLT_ID);
        Mockito.when(topologyContext.getComponentOutputFields(FlowSyncAssembler.BOLT_ID, Utils.DEFAULT_STREAM_ID))
                .thenReturn(FlowSyncAssembler.STREAM_FIELDS);

        Mockito.when(topologyContext.getComponentId(TASK_ID_FLOW_SYNC_ROUTER)).thenReturn(FlowSyncRouter.BOLT_ID);
        Mockito.when(topologyContext.getComponentOutputFields(FlowSyncRouter.BOLT_ID, Utils.DEFAULT_STREAM_ID))
                .thenReturn(FlowSyncRouter.STREAM_FIELDS);
    }

    protected FlowInfoData makeFlowInfoRecord(FlowOperation operation) {
        return makeFlowInfoRecord(FLOW_ID, ENDPOINT_ALPHA, ENDPOINT_BETA, operation);
    }

    protected FlowInfoData makeFlowInfoRecord(
            String flowId, FlowEndpointPayload endpointAlpha, FlowEndpointPayload endpointBeta,
            FlowOperation operation) {
        String description = "unit-test's flow";
        Flow forward = new Flow(
                flowId,
                1000, false, description,
                endpointAlpha.getSwitchDpId(), endpointAlpha.getPortId(), endpointAlpha.getVlanId(),
                endpointBeta.getSwitchDpId(), endpointBeta.getPortId(), endpointBeta.getVlanId());
        Flow reverse = Flow.ofPeer(forward, 0);

        CommandContext context = new CommandContext();
        return new FlowInfoData(
                flowId, new ImmutablePair<>(forward, reverse), operation, context.getCorrelationId());
    }
}
