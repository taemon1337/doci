package com.pst.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
  private final long id;
  private final String source;
  private final String folder;
  private final String from;
  private final String to;
  private final String subject;
  private final String date;
  private final String content;
  // attachments

  public Message(long id, String source, String folder, String from, String to, String subject, String date, String content) {
    this.id = id;
    this.source = source;
    this.folder = folder;
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.date = date;
    this.content = content;
  }

  @JsonProperty
  public long getId() {
    return id;
  }
  
  @JsonProperty
  public String getSource() {
      return source;
  }
  
  @JsonProperty
  public String getFolder() {
      return folder;
  }
  
  @JsonProperty
  public String getFrom() {
      return from;
  }
  
  @JsonProperty
  public String getTo() {
      return to;
  }
  
  @JsonProperty
  public String getSubject() {
      return subject;
  }
  
  @JsonProperty
  public String getDate() {
      return date;
  }

  @JsonProperty
  public String getContent() {
    return content;
  }
}