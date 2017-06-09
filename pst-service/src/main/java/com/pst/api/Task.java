package com.pst.api;

import com.pst.PstJson;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task extends PstJson {
  private final String datetime;

  public PstItem(String datetime) {
    this.datetime = datetime;
  }

  @JsonProperty
  public String getDatetime() {
    return datetime;
  }
}