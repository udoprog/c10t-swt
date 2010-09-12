package eu.toolchain;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class Main {
	private static final int RENDER_BYTE = 0x10;
	private static final int COMP_BYTE = 0x20;
	private static final int IMAGE_BYTE = 0x30;
	private static final int ERROR_BYTE = 0x01;
	
	public static final String TITLE = "c10t Graphical Interface";
	public static final String INITIAL_PROGRESS_LABEL = "Press 'Render' to render image";
	
	private static Display display;
	private static Shell shell;
	private static Text worldPath;
	private static Text outputFile;
	private static Combo mode;
	private static ProgressBar progressBar;
	private static Label progressBarLabel;
	private static Spinner coreSpinner;
	
	public static List<String> buildCommandList() {
		List<String> command = new ArrayList<String>();
		
		String platform = SWT.getPlatform();
		
		if (platform.equals("win32")) {
			command.add("c10t.exe");
		} else {
			command.add("/usr/local/bin/c10t");
		}
		
		// command for binary progress
		command.add("-x");
		
		switch (mode.getSelectionIndex()) {
			case 0: break;
			case 1: command.add("-q"); break;
			case 2: command.add("-y"); break;
		}
		
		if (!StringUtils.isEmpty(worldPath.getText())) {
			command.add("-w");
			command.add(worldPath.getText());
		}
		
		if (!StringUtils.isEmpty(outputFile.getText())) {
			command.add("-o");
			command.add(outputFile.getText());
		}
		
		command.add("-m");
		command.add(Integer.toString(coreSpinner.getSelection()));
		
		return command;
	}
	
	public static int runCommand(List<String> command) throws IOException {
		Process p = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
		
        InputStream is = p.getInputStream();
        
    	int b;
    	
    	int stage = 0x0;
    	
        while ((b = is.read()) != -1) {
          switch(b) {
          case ERROR_BYTE:
        	  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        	  throw new IOException(bufferedReader.readLine());
          case RENDER_BYTE:
        	  if (stage != RENDER_BYTE) {
        		  progressBarLabel.setText("Rendering Parts...");
        		  stage = RENDER_BYTE;
        	  }
        	  
        	  if ((b = is.read()) == -1) {
        		  throw new IOException("render byte failure");
        	  }
        	  
        	  progressBar.setSelection(((b * 100) / 0xff));
        	  break;
          case COMP_BYTE:
        	  if (stage != COMP_BYTE) {
        		  progressBarLabel.setText("Compositioning Image...");
        		  stage = COMP_BYTE;
        	  }
        	  
        	  if ((b = is.read()) == -1) {
        		  throw new IOException("comp byte failure");
        	  }
        	  
        	  progressBar.setSelection(((b * 100) / 0xff));
        	  break;
          case IMAGE_BYTE:
        	  if (stage != IMAGE_BYTE) {
        		  progressBarLabel.setText("Saving Image...");
        		  stage = IMAGE_BYTE;
        	  }
        	  
        	  if ((b = is.read()) == -1) {
        		  throw new IOException("image byte failure");
        	  }
        	  
        	  progressBar.setSelection(((b * 100) / 0xff));
        	  break;
          }
        }
        
        int r;
        
        try {
        	r = p.waitFor();
        } catch(InterruptedException e) {
        	throw new IOException("Unable to wait for command to finish", e);
        }
        
        if (r != 0) {
        	throw new IOException("Command returned non-zero status code");
        }
        
        return r;
	}
	
	public static void setupLayout() {
		GridData fill = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		GridData expand = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		
		GridData normal2 = new GridData(GridData.FILL_VERTICAL, GridData.BEGINNING, false, false);
		normal2.horizontalSpan = 2;
		
		GridData fill2 = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		fill2.horizontalSpan = 2;
		
		GridData fill3 = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		fill3.horizontalSpan = 3;
		
		GridData expand3 = new GridData(GridData.FILL_BOTH, GridData.CENTER, true, true);
		expand3.horizontalSpan = 3;
		
		GridData center3 = new GridData(GridData.FILL, GridData.CENTER, false, false);
		center3.horizontalSpan = 3;
		
		{
			final Label world = new Label(shell, SWT.NONE);
			world.setText("World: ");
			
			worldPath = new Text(shell, SWT.SINGLE | SWT.BORDER);
			worldPath.setLayoutData(fill);
			
			final Button chooseWorld = new Button(shell, SWT.PUSH);
			chooseWorld.setText("Select world...");
			chooseWorld.setLayoutData(expand);
			
			final DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.OPEN);
			
			chooseWorld.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					String path = directoryDialog.open();
					if (path != null) {
						worldPath.setText(path);
					}
				}
			});
		}
		
		{
			final Label world = new Label(shell, SWT.NONE);
			world.setText("Output: ");
			
			outputFile = new Text(shell, SWT.SINGLE | SWT.BORDER);
			outputFile.setLayoutData(fill);
			
			final Button setOutputFile = new Button(shell, SWT.PUSH);
			setOutputFile.setText("Set output file...");
			setOutputFile.setLayoutData(expand);
			
			final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			
			setOutputFile.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					String path = fileDialog.open();
					
					if (path != null) {
						outputFile.setText(path);
					}
				}
			});
		}
		
		{
			Label optionsLabel = new Label(shell, SWT.NONE);
			optionsLabel.setText("Options: ");
			optionsLabel.setLayoutData(fill3);
		}
		
		{
			new Label(shell, SWT.NONE).setText("Mode: ");
			
			mode = new Combo(shell, SWT.DROP_DOWN);
			mode.setLayoutData(fill2);
			mode.add("Normal (top-down)", 0);
			mode.add("Oblique", 1);
			mode.add("Oblique Angle", 2);
			mode.select(0); 
		}
		
		{
			new Label(shell, SWT.NONE).setText("Threads: ");
			
			coreSpinner = new Spinner(shell, SWT.BORDER);
			coreSpinner.setLayoutData(normal2);
			coreSpinner.setMaximum(24);
			coreSpinner.setMinimum(1);
			coreSpinner.setSelection(1);
		}
		
		{
			Label expander = new Label(shell, SWT.NONE);
			expander.setLayoutData(expand3);
		}
		
		{
			Button run = new Button(shell, SWT.PUSH);
			run.setLayoutData(center3);
			run.setText("Render");
			
			run.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					Runtime r = Runtime.getRuntime();
					
					List<String> cmd = buildCommandList();
					System.out.println("executing command: " + cmd);
					
					try {
						runCommand(cmd);
					} catch(IOException e) {
						progressBarLabel.setText(e.getMessage());
						progressBar.setSelection(0);
						MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
						
						messageBox.setMessage(
							"Failed to run command: " + cmd + "\n" +
							e.getMessage()
						);
						
						messageBox.open();
						return;
					}
					
					progressBar.setSelection(100);
					progressBarLabel.setText(INITIAL_PROGRESS_LABEL);
					
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
					messageBox.setMessage("Command finished successfully");
					messageBox.open();
				}
			});
		}
		
		{
			progressBarLabel = new Label(shell, SWT.NONE);
			progressBarLabel.setLayoutData(fill3);
			progressBarLabel.setText(INITIAL_PROGRESS_LABEL);
		}
		
		{
			progressBar = new ProgressBar(shell, SWT.NONE);
			progressBar.setLayoutData(fill3);
		}
	}
	
	public static void main (String [] args) {
		display = new Display();
		shell = new Shell(display);
		shell.setText(TITLE);
		shell.setMinimumSize(400, 400);
		
		setupLayout();
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		
		shell.setLayout (gridLayout);
		shell.pack ();
		shell.open ();
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();
	}
}
