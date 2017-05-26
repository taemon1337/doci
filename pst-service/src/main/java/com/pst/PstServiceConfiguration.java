package com.pst;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

public class PstServiceConfiguration extends Configuration {
  @Range(min=0,max=65535)
  private Integer port;

  @NotEmpty
  private String welcome;

  private String defaultName;
  private String uploadMessage;

  @JsonProperty
  public String getWelcome() {
    return welcome;
  }

  @JsonProperty
  public String getDefaultName() {
    return defaultName;
  }
  
  @JsonProperty
  public String getUploadMessage() {
    return uploadMessage;
  }
  
  @JsonProperty
  public void setUploadMessage(String uploadMessage) {
    this.uploadMessage = uploadMessage;
  }

  @JsonProperty
  public void setWelcome(String welcome) {
    this.welcome = welcome;
  }
  
  @JsonProperty
  public void setDefaultName(String name) {
    this.defaultName = name;
  }

  @JsonProperty
  public Integer getPort() {
    return port;
  }

  @JsonProperty
  public void setPort(Integer port) {
    this.port = port;
  }
}