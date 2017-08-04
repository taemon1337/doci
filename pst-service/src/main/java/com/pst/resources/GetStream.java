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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;
import java.lang.Thread;
import java.lang.InterruptedException;
import org.apache.commons.io.output.ChunkedOutputStream;

@Path("/gstream")
public class GetStream {
  
  @GET
  @Produces("application/octet-stream")
  public Response GetStream() throws IOException {

    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        ChunkedOutputStream output = new ChunkedOutputStream(os);
        
        for (int i = 0; i < 10 ; i++){
          try {
            String out = i + "\n";
            output.write(out.getBytes());
            output.flush();
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        
        output.flush();
      }
    };
    
    System.out.println("Streaming...");
    return Response.ok(stream).build();
  }
}
