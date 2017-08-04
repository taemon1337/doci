package com.pst.core;

import com.pff.PSTFile;
import com.pff.PSTObject;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTException;

import java.io.IOException;
// import java.io.FileNotFoundException;
import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.util.Vector;
import java.util.Date;

public class PstStreamWriter {
  private final OutputStream outputStream;

  public PstStreamWriter(OutputStream outputStream) {
      this.outputStream = outputStream;
  }
  
  public void startParsingPstFile(String filepath) {
    Executors.newSingleThreadExecutor().submit(() -> parsePstFile(filepath));
  }
  
  public void parsePstFile(String filepath) {
    try (JsonGenerator generator = Json.createGenerator(outputStream)) {
      generator.writeStartObject()
        .write("HELLO", "WORLD")
      .writeEnd();
      // try {
      //   // open the PST file
      //   PSTFile pstfile = new PSTFile(filepath);

      //   JsonGenerator arrayGenerator = generator.writeStartArray();

      //   // process the root folder
      //   processFolder(pstfile.getRootFolder(), "/", arrayGenerator);
        
      //   arrayGenerator.writeEnd();
      // } catch (PSTException | IOException e) {
      //   e.printStackTrace();
      // }
    }
  }
  
  public void processFolder(PSTFolder folder, String path, JsonGenerator generator) {
    path = path + "/" + folder.getDisplayName();
    
    if (folder.hasSubfolders()) {
      try {
        Vector<PSTFolder> childFolders = folder.getSubFolders();
        for (PSTFolder childFolder : childFolders) {
          processFolder(childFolder, path, generator);
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        generator
          .writeStartObject()
          .write("folder-path", path)
          .write("error", e.toString())
        .writeEnd();
      }
    }
    
    if (folder.getContentCount() > 0) {
      try {
        PSTObject psto = folder.getNextChild();
        while (psto != null) {
          processPstObject(psto, path, generator);
          psto = folder.getNextChild();
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        generator
          .writeStartObject()
          .write("folder-path", path)
          .write("error", e.toString())
        .writeEnd();
      }
    }
  }
  
  public void processPstObject(PSTObject obj, String path, JsonGenerator generator) {
    PSTMessage msg = (PSTMessage)obj;

    String messageClass = msg.getMessageClass();
    String messageId = msg.getInternetMessageId();
    String sender = msg.getSenderName() + "; " + msg.getSenderEmailAddress();
    String receiver = msg.getReceivedByName() + "; " + msg.getReceivedByAddress();
    String subject = msg.getSubject();
    Date datetime = msg.getMessageDeliveryTime();
    long nodeId = msg.getDescriptorNodeId();
    
    generator
      .writeStartObject()
      .write("folder-path", path)
      .write("message-class", messageClass)
      .write("message-Id", messageId)
      .write("node-Id", String.format("%d", nodeId))
      .write("sender", sender)
      .write("receiver", receiver)
      .write("subject", subject)
      .write("datetime", String.format("%d", datetime))
    .writeEnd();
  }
}
