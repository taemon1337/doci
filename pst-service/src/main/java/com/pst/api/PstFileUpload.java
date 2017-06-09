package com.pst.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class PstFileUpload {
  private final long id;
  private final String filepath;
  private final String filename;
  private final long filesize;
  private final String message;
  private final List results;

  public PstFileUpload(long id, String filepath, String filename, long filesize, String message, List results) {
    this.id = id;
    this.filepath = filepath;
    this.filename = filename;
    this.filesize = filesize;
    this.message = message;
    this.results = results;
  }

  @JsonProperty
  public long getId() {
    return id;
  }

  @JsonProperty
  public String getContent() {
    return message + ": " + filepath;
  }
  
  @JsonProperty
  public String getFilename() {
    return filename;
  }
  
  @JsonProperty
  public long getSize() {
    return filesize;
  }
  
  @JsonProperty
  public List getResults() {
    return results;
  }
}