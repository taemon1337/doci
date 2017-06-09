package com.pst.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class PstJson {
  private final String subject;

  public PstJson(String subject) {
    this.subject = subject;
  }

  @JsonProperty
  public String getSubject() {
    return subject;
  }
}