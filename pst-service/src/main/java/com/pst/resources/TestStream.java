package com.pst.resources;

import com.pst.util.FileWriter;
import com.pst.core.PstStreamReader;
import com.pst.core.PstStreamWriter;
import com.pst.core.PstStreamingOutput;

import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import com.codahale.metrics.annotation.Timed;

@Path("/stream")
@Produces("application/octet-stream")
public class TestStream {
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  public Response TestStream(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    System.out.println("File Upload Received: " + fileDetail.getFileName());

    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        
        byte[] buffer = new byte[1024];
        int read = 0;
        int count = 0;
        
        while ((read = uploadedInputStream.read(buffer, 0, buffer.length)) != -1) {
          count += 1;
          // writer.write(buffer);
          writer.write(String.format("Read %d bytes\n", count));
          writer.flush();
        }
        
        writer.write(String.format("\nTotal Chunks Read: %d\n", count));
        writer.flush();
      }
    };
    
    System.out.println("Streaming...");
    return Response.ok(stream).build();
  }
}
