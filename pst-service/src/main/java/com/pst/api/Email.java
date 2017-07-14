package com.pst.api;

import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Email extends PstJson {
  private final String sender;
  private final String receiver;

  public Email(String folder, String subject, String messageClass, Date datetime, String sender, String receiver) {
    super(folder, subject, messageClass, datetime);
    this.sender = sender;
    this.receiver = receiver;
  }
  
  @JsonProperty
  public String getSender() {
    return sender;
  }
  
  @JsonProperty
  public String getReceiver() {
    return receiver;
  }
}