package com.pst.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileUpload {
  private final long id;
  private final String filepath;
  private final String filename;
  private final long filesize;
  private final String message;

  public FileUpload(long id, String filepath, String filename, long filesize, String message) {
    this.id = id;
    this.filepath = filepath;
    this.filename = filename;
    this.filesize = filesize;
    this.message = message;
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
}