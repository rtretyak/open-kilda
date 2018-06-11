/* Copyright 2017 Telstra Open Source
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

package org.openkilda.northbound;

import org.openkilda.messaging.Message;
import org.openkilda.northbound.messaging.MessageConsumer;
import org.openkilda.northbound.messaging.MessageProducer;

import java.util.HashMap;
import java.util.Map;

/**
 * Message producer/consumer implementation for testing purposes. Processes all send/poll operations and
 * sends back prepared in advance responses by specified request id (correlation id).
 */
public class MessageExchanger implements MessageConsumer<Message>, MessageProducer {

    private Map<String, Message> responses = new HashMap<>();
    private Map<String, Message> preparedMessages = new HashMap<>();

    public MessageExchanger(Message response, String correlationId) {
        preparedMessages.put(correlationId, response);
    }

    public MessageExchanger() {
    }

    @Override
    public Message poll(String correlationId) {
        return responses.remove(correlationId);
    }

    @Override
    public void clear() {
    }

    @Override
    public void send(String topic, Message message) {
        final String requestId = message.getCorrelationId();
        if (!preparedMessages.containsKey(requestId)) {
            throw new IllegalStateException("Received unexpected request");
        } else {
            responses.put(requestId, preparedMessages.remove(requestId));
        }
    }

    public void mockResponse(Message message) {
        preparedMessages.put(message.getCorrelationId(), message);
    }

    public void resetMockedResponses() {
        preparedMessages.clear();
    }
}
