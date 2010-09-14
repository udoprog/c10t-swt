package eu.toolchain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class C10tDetachedProcess implements DetachedProcess {
  private static final int RENDER_BYTE = 0x10;
  private static final int COMP_BYTE = 0x20;
  private static final int IMAGE_BYTE = 0x30;
  private static final int ERROR_BYTE = 0x01;
  
  private C10tGraphicalInterface gui;
  
  public C10tDetachedProcess(C10tGraphicalInterface gui) {
    this.gui = gui;
  }
  
  private int read(InputStream is) throws DetachedProcessException {
    try {
      return is.read();
    } catch (IOException e) {
      throw new DetachedProcessException(e);
    }
  }
  
  private int readPercentage(InputStream is) throws DetachedProcessException {
    int b;
    if ((b = read(is)) == -1) {
      throw new DetachedProcessException("Expected percentage byte");
    }
    return ((b * 100) / 0xff);
  }
  
  private String readErrorMessage(InputStream is) throws DetachedProcessException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
      
      try {
      return bufferedReader.readLine();
    } catch (IOException e) {
      throw new DetachedProcessException(e);
    }
  }
  
  @Override
  public void run(Process p) throws DetachedProcessException {
        InputStream is = p.getInputStream();
        
    int b;
      
    int stage = 0x0;
      
    while (true) {
      if ((b = read(is)) == -1) {
        break;
      }
      
      switch(b) {
      case ERROR_BYTE:
        throw new DetachedProcessException(readErrorMessage(is));
      case RENDER_BYTE:
        if (stage != RENDER_BYTE) {
          gui.updateProgressLabel("Rendering Parts...");
          stage = RENDER_BYTE;
        }
        
        gui.updateProgressBar(readPercentage(is));
        break;
      case COMP_BYTE:
        if (stage != COMP_BYTE) {
          gui.updateProgressLabel("Compositioning Image...");
          stage = COMP_BYTE;
        }
        
        gui.updateProgressBar(readPercentage(is));
        break;
      case IMAGE_BYTE:
        if (stage != IMAGE_BYTE) {
          gui.updateProgressLabel("Saving Image...");
          stage = IMAGE_BYTE;
        }
        
        gui.updateProgressBar(readPercentage(is));
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
