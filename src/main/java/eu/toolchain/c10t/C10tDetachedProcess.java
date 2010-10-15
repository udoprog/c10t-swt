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
  private static final int PARSER_BYTE = 0x40;
  private static final int END_BYTE = 0xF0;
  private static final int ERROR_BYTE = 0x01;

  private static String PROGRESS_INITIAL = "Prepairing to render Image...";
  private static String PROGRESS_RENDER = "Rendering image...";
  private static String PROGRESS_COMP = "Compositioning image...";
  private static String PROGRESS_IMAGE = "Saving image...";

  private C10tGraphicalInterface gui;
  private Hex hex;
  private StringBuffer output;
  
  public C10tDetachedProcess(C10tGraphicalInterface gui) {
    this.gui = gui;
    this.hex = new Hex();
    this.output = new StringBuffer();
  }
  
  public String getOutput() {
    return output.toString();
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
        String error = bufferedReader.readLine();
        output.append(error);
        return error;
      } catch (IOException e) {
        throw new DetachedProcessException(e);
      }
  }

  public int nextByte(InputStream is) throws DetachedProcessException {
      byte bytes[] = new byte[2];

      try {
        is.read(bytes, 0, 2);
      } catch(IOException e) {
        throw new DetachedProcessException("Too few characters to read byte", e);
      }
      
      output.append((char)bytes[0]);
      output.append((char)bytes[1]);
      
      byte result[];

      try {
          result = hex.decode(bytes);
      } catch(DecoderException e) {
          throw new DetachedProcessException("Could not decode byte", e);
      }

      return (result[0] & 0xff);
  }

  @Override
  public void run(Process p) throws DetachedProcessException {
    gui.updateProgressLabel(PROGRESS_INITIAL);
    gui.updateProgressBar(0);

    InputStream is = p.getInputStream();

    int stage = 0x0;
    int parser_perc = 0;
    int parser_progress = 0;

    while (true) {
      int type = nextByte(is);
      
      if (type == END_BYTE) {
        break;
      }
      
      switch(type) {
      case ERROR_BYTE:
        String error = readErrorMessage(is);
        System.out.println("ERROR: " + error);
        System.out.println("Whole output following:");
        System.out.print(output.toString());
        throw new DetachedProcessException(error.substring(0, 255));
      case PARSER_BYTE:
        if (stage != PARSER_BYTE) {
          gui.updateProgressLabel("Performing broad phase scan... (" + parser_progress + "/?)");
          stage = PARSER_BYTE;
        }
        
        if (nextByte(is) == 1) {
            parser_progress += 1000;
            gui.updateProgressLabel("Performing broad phase scan... (" + parser_progress + "/?)");
            if (++parser_perc > 100) parser_perc = 0;
            gui.updateProgressBar(parser_perc);
        }
        break;
      case RENDER_BYTE:
        if (stage != RENDER_BYTE) {
          gui.updateProgressLabel(PROGRESS_RENDER);
          stage = RENDER_BYTE;
        }
        
        gui.updateProgressBar(convertPercentage(nextByte(is)));
        break;
      case IMAGE_BYTE:
        if (stage != IMAGE_BYTE) {
          gui.updateProgressLabel(PROGRESS_IMAGE);
          stage = IMAGE_BYTE;
        }
        
        gui.updateProgressBar(convertPercentage(nextByte(is)));
        break;
      default:
        throw new DetachedProcessException("Bad command byte: " + type);
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
