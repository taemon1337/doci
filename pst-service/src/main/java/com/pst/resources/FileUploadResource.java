package com.pst.resources;

import com.pst.api.FileUpload;
import com.pst.api.Message;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTAttachment;
import com.pff.PSTAppointment;
import com.pff.PSTActivity;
import com.pff.PSTTask;
import com.pff.PSTException;
import com.pff.PSTContact;
import com.pff.PSTObject;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
public class FileUploadResource {
  private final String message;
  private final AtomicLong counter;
  
  public FileUploadResource(String message) {
    this.message = message;
    this.counter = new AtomicLong();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  public FileUpload uploadFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();

		writeToFile(uploadedInputStream, uploadedFileLocation);
		parsePst(uploadedFileLocation);
		File file = new File(uploadedFileLocation);

		return new FileUpload(counter.incrementAndGet(), uploadedFileLocation, fileDetail.getFileName(), file.length(), message);
  }
  
  private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
		try {
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parsePst(String inputPath) {
		try {
			try {
				PSTFile pstFile = new PSTFile(inputPath);
    		System.out.println(pstFile.getMessageStore().getDisplayName());
    		processFolder(pstFile.getRootFolder());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (PSTException | IOException e) {
			e.printStackTrace();
		}
	}
	
	int depth = -1;

  public void processFolder(PSTFolder folder) throws PSTException, IOException {
      depth++;
      // the root folder doesn't have a display name
      if (depth > 0) {
          printDepth();
          System.out.println(folder.getDisplayName());
      }

      // go through the folders...
      if (folder.hasSubfolders()) {
          Vector<PSTFolder> childFolders = folder.getSubFolders();
          for (PSTFolder childFolder : childFolders) {
              processFolder(childFolder);
          }
      }

      if (folder.getContentCount() > 0) {
          depth++;

          PSTObject psto = folder.getNextChild();
          while (psto != null) {
            printDepth();
            processMessage(psto);
            psto = folder.getNextChild();
          }
          depth--;
      }
      depth--;
  }
  
  public void processMessage(PSTObject obj) {
    if (obj instanceof PSTContact) {
      PSTContact contact = (PSTContact)obj;
      System.out.println("CONTACT: " + contact.getDisplayName());
    } else if (obj instanceof PSTAppointment) {
      PSTAppointment appt = (PSTAppointment)obj;
      System.out.println("APPOINTMENT: " + appt.getSubject());
    } else if (obj instanceof PSTActivity) {
      PSTActivity activity = (PSTActivity)obj;
      System.out.println("ACTIVITY: " + activity.getSubject());
    } else if (obj instanceof PSTTask) {
      PSTTask task = (PSTTask)obj;
      System.out.println("TASK: " + task.getSubject());
    } else if (obj instanceof PSTMessage) {
      PSTMessage msg = (PSTMessage)obj;
        // System.out.println("MESSAGE: " + msg.getSubject());
        System.out.println("");
    } else if (obj instanceof PSTAttachment) {
      PSTAttachment attachment = (PSTAttachment)obj;
      System.out.println("ATTACHMENT: " + attachment.getFilename());
    } else {
      System.out.println("?");
    }
  }

  public void printDepth() {
      for (int x = 0; x < depth-1; x++) {
          System.out.print(" | ");
      }
      System.out.print(" |- ");
  }
}