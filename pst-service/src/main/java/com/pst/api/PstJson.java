package com.pst.api;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.MessageDigest;
import java.math.BigInteger;

public abstract class PstJson {
  public final String sha256;
  public final String folder;
  public final String subject;
  public final String messageClass;
  private final Date datetime;

  public PstJson(String folder, String subject, String messageClass, Date datetime) {
    this.sha256 = calcSha256(folder, subject, messageClass);
    this.folder = folder;
    this.subject = subject;
    this.messageClass = messageClass;
    this.datetime = datetime;
  }
  
  private String calcSha256(String ... args) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(String.join("", args).getBytes("UTF-8"));
      byte[] digest = md.digest();
      return String.format("%064x", new BigInteger(1, digest));
    } catch (Exception err) {
      return err.toString();
    }
  }

  @JsonProperty
  public String getSha256() {
    return sha256;
  }

  @JsonProperty
  public String getFolder() {
    return folder;
  }

  @JsonProperty
  public String getSubject() {
    return subject;
  }

  @JsonProperty
  public String getMessageClass() {
    return messageClass;
  }
  
  @JsonProperty
  public Date getDatetime() {
    return datetime;
  }
}