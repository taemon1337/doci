package com.pst.api;

import com.pst.PstJson;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Attachment extends PstJson {
  private final String filename;

  public Attachment(String filename) {
    this.filename = filename;
  }

  @JsonProperty
  public String getFilename() {
    return filename;
  }
}