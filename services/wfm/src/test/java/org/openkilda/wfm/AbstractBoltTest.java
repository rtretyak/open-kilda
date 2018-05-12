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

package org.openkilda.wfm;

import static org.mockito.ArgumentMatchers.eq;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBoltTest {
    @Mock
    private TopologyContext topologyContext;

    @Mock
    private OutputCollector output;

    @Before
    public void mockDependencies() {
        MockitoAnnotations.initMocks(this);
    }

    protected void initBolt(AbstractBolt bolt) {
        bolt.prepare(new Config(), topologyContext, output);
    }

    protected Values feedBolt(AbstractBolt bolt, Tuple input) {
        bolt.execute(input);

        ArgumentCaptor<Values> response = ArgumentCaptor.forClass(Values.class);
        Mockito.verify(getOutput()).emit(eq(input), response.capture());

        return response.getValue();
    }
}
