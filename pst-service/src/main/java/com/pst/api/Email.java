package com.pst.api;

import com.pst.api.PstJson;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Email extends PstJson {
  private final String sender;
  private final String receiver;
  private final String datetime;

  public PstItem(String subject, String sender, String receiver, String datetime) {
    this.subject = subject;
    this.sender = sender;
    this.receiver = receiver;
    this.datetime = datetime;
  }
  
  @JsonProperty
  public String getSender() {
    return sender;
  }
  
  @JsonProperty
  public String getReceiver() {
    return receiver;
  }
  
  @JsonProperty
  public String getDatetime() {
    return datetime;
  }
}