package com.pst.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileUpload {
  private final long id;
  private final String filepath;
  private final String filename;
  private final long filesize;
  private final String filetype;
  private final String message;

  public FileUpload(long id, String filepath, String filename, long filesize, String filetype, String message) {
    this.id = id;
    this.filepath = filepath;
    this.filename = filename;
    this.filesize = filesize;
    this.filetype = filetype;
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
}