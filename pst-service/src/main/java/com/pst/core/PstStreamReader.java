package com.pst.core;

import java.io.InputStream;
import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.InputStream;

public class PstStreamReader {
  private final InputStream inputStream;

  public PstStreamReader(InputStream inputStream) {
      this.inputStream = inputStream;
  }
  
  public void startReading() {
    JsonParser parser = Json.createParser(inputStream);
    boolean orderEnded = false;
    String output = "";

    while (parser.hasNext()) {
      JsonParser.Event event = parser.next();
      switch (event) {
        case END_ARRAY:
          if (orderEnded) {
            orderEnded = false;
            output = "";
          }
          break;
        case KEY_NAME:
          String key = parser.getString();
          if ("customer-info".equals(key)) {
            output += key + ":\n";
          } else {
            output += key + " = ";
          }
          if (key.equals("email")) {
            orderEnded = true;
          }
          break;
        case VALUE_STRING:
        case VALUE_NUMBER:
          output += parser.getString() + "\n";
          break;
      }
    }
  }

  public void startReadingRaw() {
    JsonParser parser = Json.createParser(inputStream);
    while (parser.hasNext()) {
      JsonParser.Event event = parser.next();
      switch (event) {
        case START_ARRAY:
        case END_ARRAY:
        case START_OBJECT:
        case END_OBJECT:
        case VALUE_FALSE:
        case VALUE_NULL:
        case VALUE_TRUE:
          break;
        case KEY_NAME:
          break;
        case VALUE_STRING:
        case VALUE_NUMBER:
          break;
      }
    }
  }

}
