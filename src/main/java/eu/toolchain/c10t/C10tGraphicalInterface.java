package eu.toolchain.c10t;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class C10tGraphicalInterface {
  public static final String INITIAL_PROGRESS_LABEL = "Press 'Render' to render image";
  
  private Display display;
  private Text worldPath;
  private Text outputFile;
  private Combo mode;
  private ProgressBar progressBar;
  private Label progressBarLabel;
  private Spinner coreSpinner;
  //private Spinner bottom;
  private Spinner top;
  private Spinner bottom;
  private Button renderbutton;
  private Button flip;
  private Button inverse;
  private Button cavemode;
  private Button nightmode;
  
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

    command.add("-b");
    command.add(Integer.toString(bottom.getSelection()));

    command.add("-t");
    command.add(Integer.toString(top.getSelection()));
    
    if (flip.getSelection()) {
      command.add("-f");
    }
    
    if (inverse.getSelection()) {
      command.add("-r");
    }
    
    if (cavemode.getSelection()) {
      command.add("-c");
    }
    
    if (nightmode.getSelection()) {
      command.add("-n");
    }
    
    command.add("-m");
    command.add(Integer.toString(coreSpinner.getSelection()));
    
    return command.toArray(new String[command.size()]);
  }

  private Shell setupOptionsShell() {
    final Shell shell = new Shell(display);

    shell.addListener(SWT.CLOSE, new Listener() {
      @Override
      public void handleEvent(Event event) {
        event.doit = false;
        shell.setVisible(false);
      }
    });

    shell.setVisible(false);
    shell.setLayout(new RowLayout());
    shell.setSize(200, 200);
    shell.setText("Options");

    {
      flip = new Button(shell, SWT.CHECK);
      flip.setText("Flip 90 degrees CCW");
    }

    {
      new Label(shell, SWT.NONE);

      inverse = new Button(shell, SWT.CHECK);
      inverse.setText("Flip 180 degrees CCW");
    }

    {
      new Label(shell, SWT.NONE);

      cavemode = new Button(shell, SWT.CHECK);
      cavemode.setText("Cave-mode");
    }

    {
      new Label(shell, SWT.NONE);

      nightmode = new Button(shell, SWT.CHECK);
      nightmode.setText("Night-mode");
    }

    return shell;
  }

  public C10tGraphicalInterface(final Display display, final Shell shell) {
    this.display = display;

    final Shell options = setupOptionsShell();

    Menu menuBar = new Menu(shell, SWT.BAR);
    MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    fileMenuHeader.setText("&File");

    Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
    fileMenuHeader.setMenu(fileMenu);

    MenuItem fileOptionsItem = new MenuItem(fileMenu, SWT.PUSH);
    fileOptionsItem.setText("&Options");

    fileOptionsItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent selectionEvent) {
        options.setVisible(true);
      }
    });

    MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
    fileExitItem.setText("E&xit");

    fileExitItem.addSelectionListener(new SelectionAdapter(){
      @Override
      public void widgetSelected(SelectionEvent selectionEvent) {
        shell.close();
        display.dispose();
      }
    });

    shell.setMenuBar(menuBar);

    // World input path
    {
      GridData inputGridData = new GridData();
      inputGridData.horizontalAlignment = GridData.FILL;
      inputGridData.verticalAlignment = GridData.CENTER;
      inputGridData.grabExcessHorizontalSpace = true;

      GridData buttonGridData = new GridData();
      buttonGridData.verticalAlignment = GridData.CENTER;

      // select world
      {
        new Label(shell, SWT.NONE).setText("World: ");

        worldPath = new Text(shell, SWT.SINGLE | SWT.BORDER);
        worldPath.setLayoutData(inputGridData);

        final Button worldButton = new Button(shell, SWT.PUSH);
        worldButton.setText("Select world...");
        worldButton.setLayoutData(buttonGridData);

        final DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.OPEN);

        worldButton.addSelectionListener(new SelectionAdapter() {
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
        outputFile.setLayoutData(inputGridData);

        final Button setOutputFile = new Button(shell, SWT.PUSH);
        setOutputFile.setText("Set output file...");
        setOutputFile.setLayoutData(buttonGridData);

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
    }
    
    {
      new Label(shell, SWT.NONE).setText("Mode: ");
      
      mode = new Combo(shell, SWT.DROP_DOWN);
      mode.add("Normal (top-down)", 0);
      mode.add("Oblique", 1);
      mode.add("Oblique Angle", 2);
      mode.select(0);
      
      GridData modeGridData = new GridData();
      modeGridData.horizontalAlignment = GridData.BEGINNING;
      modeGridData.horizontalSpan = 2;
      mode.setLayoutData(modeGridData);
    }
    
    {
      new Label(shell, SWT.NONE).setText("Threads: ");
      
      coreSpinner = new Spinner(shell, SWT.BORDER);
      coreSpinner.setMaximum(24);
      coreSpinner.setMinimum(1);
      coreSpinner.setSelection(1);
      new Label(shell, SWT.NONE);
    }
    
   /* {
      new Label(shell, SWT.NONE).setText("Options: ");
      
      flip = new Button(shell, SWT.CHECK);
      flip.setText("Flip 90 degrees CCW");
      flip.setLayoutData(fill2);
    }
    
    {
      new Label(shell, SWT.NONE);
      
      inverse = new Button(shell, SWT.CHECK);
      inverse.setText("Flip 180 degrees CCW");
      inverse.setLayoutData(fill2);
    }
    
    {
      new Label(shell, SWT.NONE);
      
      cavemode = new Button(shell, SWT.CHECK);
      cavemode.setText("Cave-mode");
      cavemode.setLayoutData(fill2);
    }
    
    {
      new Label(shell, SWT.NONE);
      
      nightmode = new Button(shell, SWT.CHECK);
      nightmode.setText("Night-mode");
      nightmode.setLayoutData(fill2);
    }
    
    {
      new Label(shell, SWT.NONE).setText("Limits: ");

      Composite comp = new Composite(shell, SWT.NONE);
      comp.setLayout(new GridLayout(2, true));
      comp.setLayoutData(expand2);

      bottom = new Spinner(comp, SWT.BORDER);
      bottom.setIncrement(1);
      final Label bottomLabel = new Label(comp, SWT.NONE);
      bottom.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent arg0) {
          if (bottom.getSelection() >= top.getSelection()) {
            bottom.setSelection(top.getSelection() - 1);
          }
        }
      });
      
      bottom.setMinimum(0);
      bottom.setMaximum(0x7f);

      bottomLabel.setText("Bottom");

      top = new Spinner(comp, SWT.BORDER);
      top.setIncrement(1);
      final Label topLabel = new Label(comp, SWT.NONE);
      top.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent arg0) {
          if (top.getSelection() <= bottom.getSelection()) {
            top.setSelection(bottom.getSelection() + 1);
          }
        }
      });
      top.setMinimum(0);
      top.setMaximum(0x7f);
      top.setSelection(0x7f);

      topLabel.setText("Top");

      comp.pack();
    }
    
    {
      Label expander = new Label(shell, SWT.NONE);
      expander.setLayoutData(expand3);
    }
    

    
    {
      progressBarLabel = new Label(shell, SWT.NONE);
      progressBarLabel.setLayoutData(fill3);
      progressBarLabel.setText(INITIAL_PROGRESS_LABEL);
    }
    
    {
      progressBar = new ProgressBar(shell, SWT.NONE);
      progressBar.setLayoutData(fill3);
    }*/

    {
      renderbutton = new Button(shell, SWT.PUSH);
      renderbutton.setText("Render");
    }
  }
}
