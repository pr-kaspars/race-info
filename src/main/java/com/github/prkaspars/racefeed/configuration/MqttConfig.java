package com.github.prkaspars.racefeed.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {
  private String serverURI;
  private String clientId;

  public String getServerURI() {
    return serverURI;
  }

  public void setServerURI(String serverURI) {
    this.serverURI = serverURI;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
