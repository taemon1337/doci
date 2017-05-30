package com.pst.resources;

import com.pst.api.FileUpload;
import com.pst.api.Message;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTException;

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

      // and now the emails for this folder
      if (folder.getContentCount() > 0) {
          depth++;
          PSTMessage email = (PSTMessage)folder.getNextChild();
          while (email != null) {
              printDepth();
              System.out.println("Email: FROM "+email.toString());
              email = (PSTMessage)folder.getNextChild();
          }
          depth--;
      }
      depth--;
  }

  public void printDepth() {
      for (int x = 0; x < depth-1; x++) {
          System.out.print(" | ");
      }
      System.out.print(" |- ");
  }
  
  public 
	
	// private ByteArrayOutputStream inputStreamToByteStream(InputStream inputStream) {
	// 	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	// 	int nRead;
	// 	byte[] data = new byte[16384];
		
	// 	while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
	// 		buffer.write(data, 0, nRead);
	// 	}
		
	// 	buffer.flush();
		
	// 	return buffer.toByteArray();
	// }
}