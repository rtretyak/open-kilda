package org.bitbucket.openkilda.tools.mininet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.TransportPort;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "ip",
    "port",
    "of-version"
    })

public class MininetController implements IMininetController {
  @JsonProperty("host")
  private String host;
  @JsonProperty("port")
  private Integer port;
  @JsonProperty("of-version")
  private String ofVersion;
  @JsonProperty("name")
  private String name;
  
  
  @Override
  @JsonProperty("host")
<<<<<<< HEAD
  public IMininetController setIP(String host) {
    this.host = host;
=======
  public IMininetController setIP(IPv4Address host) {
    this.host = host.toString();
>>>>>>> fd6adbbfcc4a5259b29e023345e9ed932566fa71
    return this;
  }
  
  @Override
  @JsonProperty("host")
  public String getIP() {
    return host;
  }

  @Override
  @JsonProperty("port")
  public IMininetController setPort(TransportPort port) {
    this.port = port.getPort();
    return this;
  }

  @Override
  @JsonProperty("port")
  public Integer getPort() {
    return port;
  }
  
  @Override
  @JsonProperty("of-version")
  public IMininetController setVersion(OFVersion version) {
    this.ofVersion = version.toString();
    return this;
  }
  
  @Override
  @JsonProperty("of-version")
  public String getOfVersion() {
    return ofVersion;
  }
  
  @Override
  @JsonProperty("name")
  public IMininetController setName(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @Override
  public IMininetController build() {
    return this;
  }

}