package eu.toolchain.c10t;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class C10tDetachedProcess implements DetachedProcess {
  private static final int RENDER_BYTE = 0x10;
  private static final int COMP_BYTE = 0x20;
  private static final int IMAGE_BYTE = 0x30;
  private static final int ERROR_BYTE = 0x01;

  private static String PROGRESS_INITIAL = "Prepairing to render Image...";
  private static String PROGRESS_RENDER = "Rendering image...";
  private static String PROGRESS_COMP = "Compositioning image...";
  private static String PROGRESS_IMAGE = "Saving image...";

  private C10tGraphicalInterface gui;
  private Hex hex;
  
  public C10tDetachedProcess(C10tGraphicalInterface gui) {
    this.gui = gui;
    this.hex = new Hex();
  }
  
  private int read(InputStream is) throws DetachedProcessException {
    try {
      return is.read();
    } catch (IOException e) {
      throw new DetachedProcessException(e);
    }
  }
  
  private int convertPercentage(int perc) throws DetachedProcessException {
    return ((perc * 100) / 0xff);
  }
  
  private String readErrorMessage(InputStream is) throws DetachedProcessException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
      
      try {
      return bufferedReader.readLine();
    } catch (IOException e) {
      throw new DetachedProcessException(e);
    }
  }

  public int nextByte(InputStream is) throws DetachedProcessException {
      byte bytes[] = new byte[2];

      try {
        is.read(bytes, 0, 2);
      } catch(IOException e) {
        return -1;
      }

      byte result[];

      try {
          result = hex.decode(bytes);
      } catch(DecoderException e) {
          return -1;
      }

      return (result[0] & 0xff);
  }

  @Override
  public void run(Process p) throws DetachedProcessException {
    gui.updateProgressLabel(PROGRESS_INITIAL);
    gui.updateProgressBar(0);

    InputStream is = p.getInputStream();

    int stage = 0x0;
    
    while (true) {
      int b = nextByte(is);

      if (b == -1) { break; }

      switch(b) {
      case ERROR_BYTE:
        throw new DetachedProcessException(readErrorMessage(is), true);
      case RENDER_BYTE:
        if (stage != RENDER_BYTE) {
          gui.updateProgressLabel(PROGRESS_RENDER);
          stage = RENDER_BYTE;
        }

        if ((b = nextByte(is)) == -1) {
          throw new DetachedProcessException("Expected percentage");
        }
        
        gui.updateProgressBar(convertPercentage(b));
        break;
      case COMP_BYTE:
        if (stage != COMP_BYTE) {
          gui.updateProgressLabel(PROGRESS_COMP);
          stage = COMP_BYTE;
        }

        if ((b = nextByte(is)) == -1) {
          throw new DetachedProcessException("Expected percentage");
        }
        
        gui.updateProgressBar(convertPercentage(b));
        break;
      case IMAGE_BYTE:
        if (stage != IMAGE_BYTE) {
          gui.updateProgressLabel(PROGRESS_IMAGE);
          stage = IMAGE_BYTE;
        }

        if ((b = nextByte(is)) == -1) {
          throw new DetachedProcessException("Expected percentage");
        }
        
        gui.updateProgressBar(convertPercentage(b));
        break;
      default:
        throw new DetachedProcessException("Bad command byte: " + b);
      }
    }
    
    try {
      if (p.waitFor() != 0) {
        throw new DetachedProcessException("Command returned non-zero status");
      }
    } catch(InterruptedException e) {
      throw new DetachedProcessException(e);
    }
  }
}
