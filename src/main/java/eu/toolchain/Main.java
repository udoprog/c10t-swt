package eu.toolchain;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Main {
	public static final String TITLE = "c10t Graphical Interface";
	public static final String NAME = "c10t";
	
	public static class RenderSelection extends SelectionAdapter {
		private final Shell shell;
		private final C10tGraphicalInterface gui;
		private final CommandExecutioner commandExecutioner;
		
		public RenderSelection(
				Shell shell,
				C10tGraphicalInterface gui,
				CommandExecutioner commandExecutioner) {
			super();
			this.shell = shell;
			this.gui = gui;
			this.commandExecutioner = commandExecutioner;
		}

		public void widgetSelected(SelectionEvent event) {
			DetachedProcessCallback callback = new DetachedProcessCallback() {
				@Override
				public void onException(DetachedProcessException e) {
					final Writer result = new StringWriter();
				    final PrintWriter printWriter = new PrintWriter(result);
				    
				    e.printStackTrace(printWriter);
					
				    gui.enableRenderButton();
				    gui.updateProgressBar(0);
				    gui.updateProgressLabel("Failed to execute command!");
				    
					gui.asyncExec(new Runnable() {
						
						@Override
						public void run() {
							MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
							
							messageBox.setMessage(
								"Got exception while trying to execute command:\n" + 
								result.toString()
							);
							
							messageBox.open();
						}
					});
				}
				
				public void onSuccess() {
					gui.updateProgressBar(100);
					gui.updateProgressLabel(C10tGraphicalInterface.INITIAL_PROGRESS_LABEL);
					gui.enableRenderButton();
					
					gui.asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
							messageBox.setMessage("Command finished successfully");
							messageBox.open();
						}
					});
				}
			};
			
			gui.disableRenderButton();
			
			try {
				commandExecutioner.spawn(callback, gui.buildArguments());
			} catch (CommandNotFoundException e) {
				StringBuffer stringBuffer = new StringBuffer();
				
				stringBuffer.append("Could not find command to execute in path:\n");
				
				for (File f : commandExecutioner.getPaths()) {
					stringBuffer.append("  " + f.getAbsolutePath() + "\n");
				}

				MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
				messageBox.setMessage(stringBuffer.toString());
				messageBox.open();
			}
		}
	}
	
	public static void main (String [] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText(TITLE);
		shell.setMinimumSize(400, 400);
		
		final C10tGraphicalInterface gui = new C10tGraphicalInterface(display, shell);
		final DetachedProcess detachedProcess = new C10tDetachedProcess(gui);
		final CommandExecutioner executioner = new CommandExecutioner("c10t", detachedProcess);
		
		gui.addRenderButtonListener(new RenderSelection(shell, gui, executioner));
		
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
