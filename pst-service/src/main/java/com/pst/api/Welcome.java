package com.pst.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Welcome {
  private final long id;
  private final String message;

  // public Welcome() {
  //   // deserialization
  // }

  public Welcome(long id, String message) {
    this.id = id;
    this.message = message;
  }

  @JsonProperty
  public long getId() {
    return id;
  }

  @JsonProperty
  public String getMessage() {
    return message;
  }
}