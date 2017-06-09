package com.pst.api;

import com.pst.PstJson;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact extends PstJson {
  private final String name;

  public PstItem(String name) {
    this.name = name;
  }

  @JsonProperty
  public String getName() {
    return name;
  }
}