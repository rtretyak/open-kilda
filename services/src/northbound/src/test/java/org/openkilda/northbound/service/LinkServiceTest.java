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

package org.openkilda.northbound.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.openkilda.messaging.Message;
import org.openkilda.messaging.model.NetworkEndpoint;
import org.openkilda.messaging.nbtopology.response.ChunkedInfoMessage;
import org.openkilda.messaging.nbtopology.response.LinkPropsData;
import org.openkilda.northbound.MessageExchanger;
import org.openkilda.northbound.dto.LinkPropsDto;
import org.openkilda.northbound.messaging.MessageConsumer;
import org.openkilda.northbound.messaging.MessageProducer;
import org.openkilda.northbound.service.impl.LinkServiceImpl;
import org.openkilda.northbound.utils.RequestCorrelationId;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
public class LinkServiceTest {

    private static final String EMPTY_LINK_PROPS_CORRELATION_ID = "empty link props";
    private static final String LINK_PROPS_CORRELATION_ID = "non empty link props";
    private static final LinkPropsData LINK_PROPS = new LinkPropsData(
            new NetworkEndpoint("00:00:00:00:00:00:00:01", 1), new NetworkEndpoint("00:00:00:00:00:00:00:02", 2),
            Collections.singletonMap("cost", "2"));

    private static final Map<String, Message> responses = ImmutableMap.of(
            EMPTY_LINK_PROPS_CORRELATION_ID, new ChunkedInfoMessage(null, 0, EMPTY_LINK_PROPS_CORRELATION_ID, null),
            LINK_PROPS_CORRELATION_ID, new ChunkedInfoMessage(LINK_PROPS, 0, LINK_PROPS_CORRELATION_ID, null)
    );

    @Autowired
    private LinkService linkService;

    @Test
    public void shouldGetEmptyPropsList() {
        RequestCorrelationId.create(EMPTY_LINK_PROPS_CORRELATION_ID);
        List<LinkPropsDto> result = linkService.getLinkProps(null, 0, null, 0);
        assertTrue("List of link props should be empty", result.isEmpty());
    }

    @Test
    public void shouldGetPropsList() {
        RequestCorrelationId.create(LINK_PROPS_CORRELATION_ID);
        List<LinkPropsDto> result = linkService.getLinkProps(null, 0, null, 0);
        assertFalse("List of link props should be empty", result.isEmpty());

        LinkPropsDto dto = result.get(0);
        assertThat(dto.getSrcSwitch(), is(LINK_PROPS.getSource().getSwitchDpId()));
        assertThat(dto.getSrcPort(), is(LINK_PROPS.getSource().getPortId()));
        assertThat(dto.getDstSwitch(), is(LINK_PROPS.getDestination().getSwitchDpId()));
        assertThat(dto.getDstPort(), is(LINK_PROPS.getDestination().getPortId()));
    }

    @TestConfiguration
    @ComponentScan({
            "org.openkilda.northbound.converter",
            "org.openkilda.northbound.utils"})
    @PropertySource({"classpath:northbound.properties"})
    static class Config {

        @Bean
        public MessageExchanger messageExchanger() {
            return new MessageExchanger(LinkServiceTest.responses);
        }

        @Bean
        public MessageConsumer messageConsumer(MessageExchanger messageExchanger) {
            return messageExchanger;
        }

        @Bean
        public MessageProducer messageProducer(MessageExchanger messageExchanger) {
            return messageExchanger;
        }

        @Bean
        public RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        public LinkService linkService() {
            return new LinkServiceImpl();
        }
    }

}
