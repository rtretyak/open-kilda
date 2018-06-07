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

package org.openkilda.northbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkPropsDto {

    @JsonProperty("src_switch")
    private String srcSwitch;
    @JsonProperty("src_port")
    private int srcPort;
    @JsonProperty("dst_switch")
    private String dstSwitch;
    @JsonProperty("dst_port")
    private int dstPort;
    @JsonProperty("props")
    private Map<String, String> props;

    public LinkPropsDto(@JsonProperty("src_switch") String srcSwitch, @JsonProperty("src_port") int srcPort,
                        @JsonProperty("dst_switch") String dstSwitch, @JsonProperty("dst_port") int dstPort,
                        @JsonProperty("props") Map<String, String> props) {
        this.srcSwitch = srcSwitch;
        this.srcPort = srcPort;
        this.dstSwitch = dstSwitch;
        this.dstPort = dstPort;
        this.props = props;
    }

    public String getSrcSwitch() {
        return srcSwitch;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public String getDstSwitch() {
        return dstSwitch;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getProperty(String key) {
        return props.get(key);
    }
}
