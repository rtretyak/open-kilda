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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.openkilda.messaging.info.event.FlowSync;
import org.openkilda.messaging.info.event.FlowSyncCommand;
import org.openkilda.messaging.info.flow.FlowInfoData;
import org.openkilda.messaging.info.flow.FlowOperation;
import org.openkilda.messaging.model.BidirectionalFlow;
import org.openkilda.messaging.model.Flow;
import org.openkilda.messaging.model.ImmutablePair;
import org.openkilda.messaging.payload.flow.FlowEndpointPayload;
import org.openkilda.messaging.payload.flow.FlowState;
import org.openkilda.wfm.CommandContext;

import org.apache.storm.tuple.MessageId;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class FlowSyncAssemblerTest extends AbstractFlowSyncTest {
    private FlowSyncAssembler subject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        subject = new FlowSyncAssembler();
        initBolt(subject);
    }

    @Test
    public void testCreate() throws Exception {
        twoStepCase(FlowOperation.CREATE, FlowState.ALLOCATED, FlowState.UP, FlowSyncCommand.CREATE);
    }

    @Test
    public void testPushPropagate() throws Exception {
        twoStepCase(FlowOperation.PUSH_PROPAGATE, FlowState.ALLOCATED, FlowState.UP, FlowSyncCommand.CREATE);
    }

    @Test
    public void testUpdate() throws Exception {
        twoStepCase(FlowOperation.UPDATE, FlowState.UP, FlowState.UP, FlowSyncCommand.UPDATE);
    }

    @Test
    public void testDelete() throws Exception {
        oneStepCase(FlowOperation.DELETE, FlowState.UP, FlowSyncCommand.DELETE);
    }

    @Test
    public void testUnpushPropagate() throws Exception {
        twoStepCase(FlowOperation.UNPUSH_PROPAGATE, FlowState.UP, FlowState.UP, FlowSyncCommand.DELETE);
    }

    @Test
    public void testPush() throws Exception {
        oneStepCase(FlowOperation.PUSH, FlowState.UP, FlowSyncCommand.CREATE);
    }

    @Test
    public void testUnpush() throws Exception {
        oneStepCase(FlowOperation.UNPUSH, FlowState.UP, FlowSyncCommand.DELETE);
    }

    private void twoStepCase(
            FlowOperation operation, FlowState stateOrigin, FlowState stateCommit, FlowSyncCommand syncCommand)
            throws Exception {
        final BidirectionalFlow flowOrigin = makeFlow(stateOrigin, FLOW_ID, ENDPOINT_ALPHA, ENDPOINT_BETA);
        final Tuple inputOrigin = makeInput(flowOrigin, operation);

        subject.execute(inputOrigin);
        Mockito.verify(getOutput(), Mockito.times(0)).emit(eq(inputOrigin), any());

        final BidirectionalFlow flowCommit = updateFlowState(flowOrigin, stateCommit);
        final Tuple inputCommit = makeInput(flowCommit, FlowOperation.STATE);

        subject.execute(inputCommit);
        ArgumentCaptor<Values> response = ArgumentCaptor.forClass(Values.class);
        Mockito.verify(getOutput()).emit(eq(inputCommit), response.capture());

        Values output = response.getValue();
        FlowSync syncRecord = new FlowSync(syncCommand, flowCommit);
        Assert.assertEquals(syncRecord, output.get(0));

        Assert.assertEquals(inputCommit.getValueByField(FlowSyncAssembler.FIELD_ID_CONTEXT), output.get(1));
    }

    private void oneStepCase(FlowOperation operation, FlowState state, FlowSyncCommand syncCommand) {
        final BidirectionalFlow flow = makeFlow(state, FLOW_ID, ENDPOINT_ALPHA, ENDPOINT_BETA);
        Tuple input = makeInput(flow, operation);

        Values output = feedBolt(subject, input);

        FlowSync syncRecord = new FlowSync(syncCommand, flow);
        Assert.assertEquals(syncRecord, output.get(0));

        Assert.assertEquals(input.getValueByField(FlowSyncAssembler.FIELD_ID_CONTEXT), output.get(1));
    }

    private BidirectionalFlow makeFlow(
            FlowState state, String flowId, FlowEndpointPayload endpointAlpha, FlowEndpointPayload endpointBeta) {
        Flow forward = new Flow(
                flowId, 1000, false, flowId + " description",
                endpointAlpha.getSwitchDpId(), endpointAlpha.getPortId(), endpointAlpha.getVlanId(),
                endpointBeta.getSwitchDpId(), endpointBeta.getPortId(), endpointBeta.getVlanId());
        forward.setState(state);
        Flow reverse = Flow.ofPeer(forward, 0);

        return new BidirectionalFlow(forward, reverse);
    }

    private Tuple makeInput(BidirectionalFlow flow, FlowOperation operation) {
        CommandContext context = new CommandContext();
        ImmutablePair<Flow, Flow> flowPair = new ImmutablePair<>(flow.getForward(), flow.getReverse());
        FlowInfoData payload = new FlowInfoData(flow.getFlowId(), flowPair, operation, context.getCorrelationId());

        MessageId anchor = MessageId.makeUnanchored();
        return new TupleImpl(
                getTopologyContext(), new Values(payload.getFlowId(), payload, context),
                TASK_ID_FLOW_SYNC_ROUTER, Utils.DEFAULT_STREAM_ID, anchor);
    }

    private BidirectionalFlow updateFlowState(BidirectionalFlow flow, FlowState state) {
        Flow halfFlow = flow.getForward();
        halfFlow.setState(state);
        return new BidirectionalFlow(halfFlow, Flow.ofPeer(halfFlow, 0));
    }
}
