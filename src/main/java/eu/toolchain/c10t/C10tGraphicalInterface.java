package eu.toolchain.c10t;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class C10tGraphicalInterface {
  public static final String INITIAL_PROGRESS_LABEL = "Prepairing to render image...";
  public static final String EXCLUDE_BLOCKS = "Exclude Blocks";
  public static final String INCLUDE_BLOCKS = "Include Blocks";

  private Display display;
  private Shell progressShell;
  private Text worldPath;
  private Text outputFile;
  private Combo mode;
  private ProgressBar progressBar;
  private Label progressBarLabel;
  private Button coreEnabledButton;
  private Spinner coreSpinner;
  private Button memoryLimitEnabledButton;
  private Spinner memoryLimitSpinner;
  //private Spinner bottom;
  private Spinner top;
  private Spinner bottom;
  private Button renderbutton;
  private Button cavemode;
  private Button nightmode;
  private Button heightmap;
  private Button hideall;
  private Button limitButton;
  private org.eclipse.swt.widgets.List excludeIncludeList;
  
  public void addRenderButtonListener(SelectionListener listener) {
    renderbutton.addSelectionListener(listener);
  }
  
  public void asyncExec(final Runnable run) {
    display.asyncExec(run);
  }
  
  public void enableRenderButton() {
    display.asyncExec(new Runnable() {
      public void run() {
        progressShell.setVisible(false);
        renderbutton.setEnabled(true);
      }
    });
  }
  
  public void disableRenderButton() {
    display.asyncExec(new Runnable() {
      public void run() {
        progressShell.setVisible(true);
        progressShell.forceActive();
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

  private String getDefaultOutputFile() {
      return new File(System.getProperty("user.dir"), "out.png").getAbsolutePath();
  }
  
  public String[] buildArguments() throws CommandNotFoundException {
    List<String> command = new ArrayList<String>();
    
    // command for binary progress
    command.add("-x");
    
    switch (mode.getSelectionIndex()) {
      case 0: break;
      case 1: command.add("-q"); break;
      case 2: command.add("-y"); break;
      case 3: command.add("-z"); break;
    }
    
    if (!StringUtils.isEmpty(worldPath.getText())) {
      command.add("-w");
      command.add(worldPath.getText());
    }
    
    if (StringUtils.isEmpty(outputFile.getText())) {
        outputFile.setText(getDefaultOutputFile());
    }

    command.add("-o");
    command.add(outputFile.getText());

    command.add("-b");
    command.add(Integer.toString(bottom.getSelection()));

    command.add("-t");
    command.add(Integer.toString(top.getSelection()));
    
    if (cavemode.getSelection()) {
      command.add("-c");
    }
    
    if (nightmode.getSelection()) {
      command.add("-n");
    }
    
    if (heightmap.getSelection()) {
      command.add("--heightmap");
    }

    if (hideall.getSelection()) {
      command.add("-a");

      for (int index : excludeIncludeList.getSelectionIndices()) {
        command.add("-i");
        command.add(Integer.toString(index));
      }
    } else {
      for (int index : excludeIncludeList.getSelectionIndices()) {
        command.add("-e");
        command.add(Integer.toString(index));
      }
    }
    
    if (coreEnabledButton.getSelection()) {
    	command.add("-m");
      command.add(Integer.toString(coreSpinner.getSelection()));
    }
    
    if (memoryLimitEnabledButton.getSelection()) {
    	command.add("-M");
      command.add(Integer.toString(memoryLimitSpinner.getSelection()));
    }
    
    return command.toArray(new String[command.size()]);
  }

  private void setupGeneralOptions(Composite shell) {
    {
      Composite comp = new Composite(shell, SWT.NONE);
      RowLayout rowLayout = new RowLayout();
      rowLayout.center = true;
      rowLayout.marginLeft = 0;
      rowLayout.marginRight = 0;
      rowLayout.marginTop = 0;
      rowLayout.marginBottom = 0;
      rowLayout.spacing = 4;
      comp.setLayout(rowLayout);

      coreEnabledButton = new Button(comp, SWT.CHECK);
      coreEnabledButton.setText("Threads: ");
      RowData rowData = new RowData();
      rowData.width = 100;
      coreEnabledButton.setLayoutData(rowData);

      coreSpinner = new Spinner(comp, SWT.BORDER);
      coreSpinner.setMaximum(24);
      coreSpinner.setMinimum(1);
      coreSpinner.setSelection(1);
      comp.pack();
    }

    {
      Composite comp = new Composite(shell, SWT.NONE);
      RowLayout rowLayout = new RowLayout();
      rowLayout.center = true;
      rowLayout.marginLeft = 0;
      rowLayout.marginRight = 0;
      rowLayout.marginTop = 0;
      rowLayout.marginBottom = 0;
      rowLayout.spacing = 4;
      comp.setLayout(rowLayout);

      limitButton = new Button(comp, SWT.CHECK);
      limitButton.setText("Limits: ");

      RowData rowData = new RowData();
      rowData.width = 100;
      limitButton.setLayoutData(rowData);

      bottom = new Spinner(comp, SWT.BORDER);
      bottom.setIncrement(1);
      bottom.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent arg0) {
          if (bottom.getSelection() >= top.getSelection()) {
            bottom.setSelection(top.getSelection() - 1);
          }
        }
      });

      bottom.setMinimum(0);
      bottom.setMaximum(0x7f);

      new Label(comp, SWT.NONE).setText("-");

      top = new Spinner(comp, SWT.BORDER);
      top.setIncrement(1);
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

      comp.pack();
    }

    {
      Composite comp = new Composite(shell, SWT.NONE);
      RowLayout rowLayout = new RowLayout();
      rowLayout.center = true;
      rowLayout.marginLeft = 0;
      rowLayout.marginRight = 0;
      rowLayout.marginTop = 0;
      rowLayout.marginBottom = 0;
      rowLayout.spacing = 4;
      comp.setLayout(rowLayout);

      memoryLimitEnabledButton = new Button(comp, SWT.CHECK);
      memoryLimitEnabledButton.setText("Memory Limit: ");
      RowData rowData = new RowData();
      rowData.width = 100;
      memoryLimitEnabledButton.setLayoutData(rowData);

      memoryLimitSpinner = new Spinner(comp, SWT.BORDER);
      memoryLimitSpinner.setMaximum(1024 * 36);
      memoryLimitSpinner.setMinimum(32);
      memoryLimitSpinner.setSelection(1024);
      comp.pack();
    }
  }

  private void setupRenderingOptions(Composite shell) {
    {
      cavemode = new Button(shell, SWT.CHECK);
      cavemode.setText("Cave mode");
    }

    {
      nightmode = new Button(shell, SWT.CHECK);
      nightmode.setText("Night mode");
    }
    
    {
      heightmap = new Button(shell, SWT.CHECK);
      heightmap.setText("Height map");
    }

    final Label includeExcludeLabel = new Label(shell, SWT.NONE);

    includeExcludeLabel.setText(EXCLUDE_BLOCKS);

    {
      excludeIncludeList = new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
      GridData gridData = new GridData();
      gridData.heightHint = 160;
      gridData.verticalAlignment = SWT.FILL;
      excludeIncludeList.setLayoutData(gridData);

      for (Material m : EnumSet.allOf(Material.class)) {
        excludeIncludeList.add("");
      }

      for (Material m : EnumSet.allOf(Material.class)) {
        excludeIncludeList.setItem(m.getCode(), m.name());
      }
    }

    {
      hideall = new Button(shell, SWT.CHECK);
      hideall.setText("Hide all");

      hideall.addSelectionListener(new SelectionAdapter(){
        @Override
        public void widgetSelected(SelectionEvent selectionEvent) {
          if (hideall.getSelection()) {
            includeExcludeLabel.setText(INCLUDE_BLOCKS);
          } else {
            includeExcludeLabel.setText(EXCLUDE_BLOCKS);
          }
        }
      });
    }
  }

  private Shell setupOptionsShell() {
    final Shell shell = new Shell(display, SWT.MIN | SWT.MAX | SWT.TITLE);

    shell.addListener(SWT.Close, new Listener() {
      @Override
      public void handleEvent(Event event) {
        event.doit = false;
        shell.setVisible(false);
      }
    });

    shell.setVisible(false);

    GridLayout shellLayout = new GridLayout();
    shellLayout.marginTop = 8;
    shellLayout.marginBottom = 8;
    shellLayout.marginLeft = 8;
    shellLayout.marginRight = 8;
    shellLayout.horizontalSpacing = 8;
    shellLayout.verticalSpacing = 8;
    shellLayout.makeColumnsEqualWidth = true;
    shellLayout.numColumns = 2;

    GridLayout gridLayout = new GridLayout();
    gridLayout.marginTop = 4;
    gridLayout.marginBottom = 4;
    gridLayout.marginLeft = 4;
    gridLayout.marginRight = 4;
    gridLayout.verticalSpacing = 0;
    gridLayout.verticalSpacing = 4;
    gridLayout.makeColumnsEqualWidth = true;

    shell.setLayout(shellLayout);
    shell.setText("c10t - Options");

    Group general = new Group(shell, SWT.SHADOW_ETCHED_IN);

    {
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.verticalAlignment = SWT.FILL;
      general.setLayout(gridLayout);
      general.setLayoutData(gridData);
      general.setText("General Options");
    }

    Group rendering = new Group(shell, SWT.SHADOW_ETCHED_IN);

    {
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.verticalAlignment = SWT.FILL;
      rendering.setLayout(gridLayout);
      rendering.setLayoutData(gridData);
      rendering.setText("Rendering Options");
    }

    setupGeneralOptions(general);
    setupRenderingOptions(rendering);

    {
      Button apply = new Button(shell, SWT.PUSH);
      apply.setText("Apply");

      apply.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent selectionEvent) {
          shell.setVisible(false);
        }
      });
    }

    general.pack();
    rendering.pack();
    shell.pack();
    return shell;
  }

  public Shell setupProgressShell() {
    final Shell shell = new Shell(display, SWT.MIN | SWT.MAX | SWT.TITLE);

    FillLayout fillLayout = new FillLayout();
    fillLayout.type = SWT.VERTICAL;
    fillLayout.marginHeight = 10;
    fillLayout.marginWidth = 10;
    shell.setLayout(fillLayout);

    shell.addListener(SWT.Close, new Listener() {
      @Override
      public void handleEvent(Event event) {
        event.doit = false;
        shell.setVisible(false);
      }
    });

    shell.setVisible(false);

    {
      progressBarLabel = new Label(shell, SWT.NONE);
      progressBarLabel.setText(INITIAL_PROGRESS_LABEL);
    }

    {
      progressBar = new ProgressBar(shell, SWT.NONE);
    }

    shell.pack();

    Point p = shell.getSize();
    shell.setSize(300, p.y);
    Rectangle r = display.getClientArea();
    shell.setLocation(r.width / 2 - 150, r.height / 2 - p.y / 2);
    return shell;
  }

  public C10tGraphicalInterface(final Display display, final Shell shell) {
    this.display = display;

    final Shell options = setupOptionsShell();
    progressShell = setupProgressShell();
    
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
        options.forceActive();
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

    {
      final ImageData logoImagedata = new ImageData(getClass().getResourceAsStream("/logo.png"));
      Canvas canvas = new Canvas(shell, SWT.NONE);

      GridData logoGridData = new GridData();
      logoGridData.horizontalAlignment = GridData.CENTER;
      logoGridData.verticalAlignment = GridData.FILL;
      logoGridData.horizontalSpan = 3;
      logoGridData.heightHint = 68;
      logoGridData.widthHint = 200;
      
      canvas.setLayoutData(logoGridData);

      canvas.addPaintListener(new PaintListener() {
        public void paintControl(PaintEvent e) {
          Image image = null;
          try {
            image = new Image(display, logoImagedata);

          } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }

          e.gc.drawImage(image, 0, 0);

          image.dispose();
        }
      });
    }

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
        outputFile.setText(getDefaultOutputFile());

        final Button setOutputFile = new Button(shell, SWT.PUSH);
        setOutputFile.setText("Set output file...");
        setOutputFile.setLayoutData(buttonGridData);

        final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);

        String[] filterExt = { "*.png" };
        fileDialog.setFilterExtensions(filterExt);

        setOutputFile.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent arg0) {
            String path = fileDialog.open();

            if (StringUtils.isEmpty(path)) {
              return;
            }

            if (!path.toUpperCase().endsWith(".PNG")) {
              path += ".png";
            }

            fileDialog.setFileName(path);
            outputFile.setText(path);
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
      mode.add("Isometric", 3);
      mode.select(0);
      
      GridData modeGridData = new GridData();
      modeGridData.horizontalAlignment = GridData.BEGINNING;
      modeGridData.horizontalSpan = 2;
      mode.setLayoutData(modeGridData);
    }

    {
      new Label(shell, SWT.NONE);
    	
      GridData renderGridData = new GridData();
      renderGridData.horizontalAlignment = GridData.END;
      renderGridData.horizontalSpan = 2;
      renderGridData.heightHint = 40;
      renderGridData.widthHint = 100;
      
      renderbutton = new Button(shell, SWT.PUSH);
      renderbutton.setText("Render");
      renderbutton.setLayoutData(renderGridData);
      renderbutton.setAlignment(SWT.CENTER);
    }
  }
}
