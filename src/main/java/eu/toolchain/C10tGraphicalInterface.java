package eu.toolchain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class C10tGraphicalInterface {
	public static final String INITIAL_PROGRESS_LABEL = "Press 'Render' to render image";
	
	private Display display;
	private Text worldPath;
	private Text outputFile;
	private Combo mode;
	private ProgressBar progressBar;
	private Label progressBarLabel;
	private Spinner coreSpinner;
	private Button renderbutton;
	private Button flip;
	private Button inverse;
	
	public void addRenderButtonListener(SelectionListener listener) {
		renderbutton.addSelectionListener(listener);
	}
	
	public void asyncExec(final Runnable run) {
		display.asyncExec(run);
	}
	
	public void enableRenderButton() {
		display.asyncExec(new Runnable() {
            public void run() {
            	renderbutton.setEnabled(true);
            }
        });
	}
	
	public void disableRenderButton() {
		display.asyncExec(new Runnable() {
            public void run() {
            	renderbutton.setEnabled(false);
            }
        });
	}
	
	public void updateProgressBar(final int progress) {
		display.asyncExec(new Runnable() {
            public void run() {
            	progressBar.setSelection(progress);
            }
        });
	}
	
	public void updateProgressLabel(final String text) {
		display.asyncExec(new Runnable() {
            public void run() {
            	progressBarLabel.setText(text);
            }
        });
	}
	
	
	public String[] buildArguments() throws CommandNotFoundException {
		List<String> command = new ArrayList<String>();
		
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
		
		if (flip.getSelection()) {
			command.add("-f");
		}
		
		if (inverse.getSelection()) {
			command.add("-r");
		}
		
		command.add("-m");
		command.add(Integer.toString(coreSpinner.getSelection()));
		
		return command.toArray(new String[command.size()]);
	}
	
	public C10tGraphicalInterface(Display display, Shell shell) {
		this.display = display;
		
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
			new Label(shell, SWT.NONE).setText("Flip Rendering: ");
			
			flip = new Button(shell, SWT.CHECK);
			flip.setText("Flip 90 degrees CCW");
			flip.setLayoutData(normal2);
		}
		
		{
			new Label(shell, SWT.NONE);
			
			inverse = new Button(shell, SWT.CHECK);
			inverse.setText("Flip 180 degrees CCW");
			inverse.setLayoutData(normal2);
		}
		
		{
			Label expander = new Label(shell, SWT.NONE);
			expander.setLayoutData(expand3);
		}
		
		{
			renderbutton = new Button(shell, SWT.PUSH);
			renderbutton.setLayoutData(center3);
			renderbutton.setText("Render");
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
}
