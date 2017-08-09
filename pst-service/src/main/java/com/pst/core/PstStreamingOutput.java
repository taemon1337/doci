package com.pst.core;

import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import javax.ws.rs.WebApplicationException;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.stream.RawField;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MessageBuilder;
import org.apache.james.mime4j.dom.field.ParseException;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.dom.FieldParser;
import org.apache.james.mime4j.MimeException;

// import org.reflections.ReflectionUtils.getAllMethods;
// import org.reflections.ReflectionUtils.withModifier;
// import org.reflections.ReflectionUtils.withPrefix;

import com.pff.PSTFile;
import com.pff.PSTObject;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTException;

import java.io.File;
import java.util.concurrent.Executors;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.util.Vector;
import java.util.Date;

public class PstStreamingOutput implements StreamingOutput {
  private final String filepath;
  
  public PstStreamingOutput(String filepath) {
    this.filepath = filepath;
  }

  public void write(OutputStream outputStream) throws IOException, WebApplicationException {
    try {
      MessageWriter writer = new DefaultMessageWriter();
      // Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));

      // open the PST file
      System.out.println("INFO: parsing PST file at" + this.filepath);
      PSTFile pstfile = new PSTFile(this.filepath);

      // process the root folder
      parseFolder(pstfile.getRootFolder(), ".", writer, outputStream);
    } catch (PSTException e) {
      e.printStackTrace();
    }
  }

  public void parseFolder(PSTFolder folder, String path, MessageWriter writer, OutputStream outputStream) {
    path = path + "." + folder.getDisplayName();
    
    if (folder.hasSubfolders()) {
      try {
        Vector<PSTFolder> childFolders = folder.getSubFolders();
        for (PSTFolder childFolder : childFolders) {
          parseFolder(childFolder, path, writer, outputStream);
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        System.out.println(String.format("Error parsing folder %s: %s", path, e.toString()));
      }
    }
    
    if (folder.getContentCount() > 0) {
      try {
        PSTObject psto = folder.getNextChild();
        while (psto != null) {
          parsePstObject(psto, path, writer, outputStream);
          psto = folder.getNextChild();
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        System.out.println(String.format("Error parsing folder %s: %s", path, e.toString()));
      }
    }
  }
  
  public void parsePstObject(PSTObject obj, String path, MessageWriter writer, OutputStream outputStream) {
    PSTMessage msg = (PSTMessage)obj;
    FieldParser parser = new DefaultFieldParser().getParser();

    String messageClass = msg.getMessageClass();
    String messageId = msg.getInternetMessageId();
    String senderAddress = msg.getSenderEmailAddress();
    String receiverAddress = msg.getReceivedByAddress();
    String sentAddress = msg.getSentRepresentingEmailAddress();
    String senderName = parseNameAddress(msg.getSenderName());
    String receiverName = parseNameAddress(msg.getReceivedByName());
    String sentName = parseNameAddress(msg.getSentRepresentingName());
    String subject = msg.getSubject();
    Date datetime = msg.getMessageDeliveryTime();
    String nodeId = String.format("%d", msg.getDescriptorNodeId());
    String body = msg.getBody();
    String senderAddressType = msg.getSenderAddrtype();
    String receiverAddressType = msg.getReceivedByAddressType();
    String sentAddressType = msg.getSentRepresentingAddressType();
    String transportHeaders = msg.getTransportMessageHeaders();
    
    body += "\n----------ORIGINAL TRANSPORT HEADERS----------\n" + transportHeaders + "\n----------END TRANSPORT HEADERS----------\n\n";

    // for now, skip all non IPM.Note classes
    if (messageClass.startsWith("IPM.Note")) {
      // Set<Method> getters = getAllMethods(obj, withModifier(Modifier.PUBLIC), withPrefix("get"));
      try {
        Message message = MessageBuilder.create()
          .setFrom(senderName)
          .setTo(receiverName)
          .setSubject(msg.getSubject())
          .setDate(msg.getMessageDeliveryTime())
          .generateMessageId(String.format("%d", msg.getDescriptorNodeId()))
          .setBody(body, Charsets.ISO_8859_1)
          .build()
        ;
        
        Header header = message.getHeader();

        // header.setField(new RawField("Transport-Headers", transportHeaders));
        header.setField(new RawField("Pst-Path", path));
        header.setField(new RawField("Node-Id", nodeId));
        header.setField(new RawField("Message-Class", messageClass));
        header.setField(new RawField("Sender-Address-Type", senderAddressType));
        header.setField(new RawField("Receiver-Address-Type", receiverAddressType));
        header.setField(new RawField("Sent-Address-Type", sentAddressType));
        header.setField(new RawField("Sender-Name", senderName));
        header.setField(new RawField("Receiver-Name", receiverName));
        header.setField(new RawField("Sent-Name", sentName));
        header.setField(new RawField("Sender-Address", senderAddress));
        header.setField(new RawField("Receiver-Address", receiverAddress));
        header.setField(new RawField("Sent-Address", sentAddress));

        writer.writeMessage(message, outputStream);
        // writer.flush();
      } catch (ParseException | IOException e) {
        e.printStackTrace();
        System.out.println(String.format("Error building MIME message %s: %s", path, e.toString()));
      }
    } else {
      System.out.println(String.format("Skipping Message Class %s", messageClass));
    }
  }

  public String parseNameAddress(String str) {
    if (str.contains("\\")) {
      String[] bits = str.split("\\");
      return bits[bits.length - 1];
    } else {
      return str;
    }
  }
}
