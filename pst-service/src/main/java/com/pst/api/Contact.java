package com.pst.api;

import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact extends PstJson {
  private final String name;

  public Contact(String folder, String name, String messageClass, Date datetime) {
    super(folder, name, messageClass, datetime);
    this.name = name;
  }

  @JsonProperty
  public String getName() {
    return name;
  }
}