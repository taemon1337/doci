package com.pst.api;

import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Attachment extends PstJson {
  private final String filename;

  public Attachment(String folder, String filename, String messageClass, Date datetime) {
    super(folder, filename, messageClass, datetime);
    this.filename = filename;
  }

  @JsonProperty
  public String getFilename() {
    return filename;
  }
}