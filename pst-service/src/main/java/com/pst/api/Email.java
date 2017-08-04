package com.pst.api;

import com.pff.PSTObject;
import com.pff.PSTMessage;
import com.pff.PSTException;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.field.ParseException;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.message.BodyPartBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MessageBuilder;
import org.apache.james.mime4j.message.MultipartBuilder;

import com.pst.api.PstJson;
import com.pst.util.Sha;

import java.io.IOException;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Email {
  public final Message message;

  public Email(PSTObject obj, String folder) throws IOException, ParseException {
    PSTMessage msg = (PSTMessage)obj;
    String messageClass = msg.getMessageClass();
    String mid = msg.getInternetMessageId();
    String sender = msg.getSenderName() + "; " + msg.getSenderEmailAddress();
    String receiver = msg.getReceivedByName() + "; " + msg.getReceivedByAddress();
    String subject = msg.getSubject();
    Date datetime = msg.getMessageDeliveryTime();
    long nid = msg.getDescriptorNodeId();

    Message message = MessageBuilder.create()
      .setFrom(sender)
      .setTo(receiver)
      .setSubject(subject)
      .setDate(datetime)
      .setMessageId(mid)
      .setBody(parseBody(msg), Charsets.ISO_8859_1)
      .build()
    ;
    
    Header header = message.getHeader();

    header.setField(new RawField("Node-Id", String.format ("%d", nid)));
    header.setField(new RawField("Message-Class", messageClass));
    // header.setField(new RawField('SHA256', Sha.digest('SHA-256', sender, receiver);
    
    // if (messageClass.startsWith("IPM.Note")) {
    //   message.setBody(parseBody(msg), Charsets.ISO_8859_1)
    // }
    this.message = message;
  }

  private String parseBody(PSTMessage msg) {
    try {
      String body = msg.getBody();
      String html = msg.getBodyHTML();
      String rtfb = msg.getRTFBody();
      int bl = body.length();
      int hl = html.length();
      int rl = rtfb.length();
      if (bl > hl && bl > rl) {
        return body;
      } else if (rl > hl && rl > bl) {
        return rtfb;
      } else {
        return html;
      }
    } catch (PSTException e) {
      e.printStackTrace();
      return "";
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

  // @JsonProperty
  // public String getMessage() {
  //   try {
  //     MessageWriter writer = new DefaultMessageWriter();
  //     String out = "";
  //     writer.writeMessage(message, out);
  //     return out;
  //   } finally {
  //     message.dispose();
  //   }
  // }

}