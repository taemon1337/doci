package com.pst.util;

import java.security.MessageDigest;
import java.math.BigInteger;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Sha {

  public static String digest(String cipher, String ... args) throws IOException {
    try {
      MessageDigest md = MessageDigest.getInstance(cipher);
      md.update(String.join("", args).getBytes("UTF-8"));
      byte[] digest = md.digest();
      return String.format("%064x", new BigInteger(1, digest));
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return e.toString();
    }
  }

}