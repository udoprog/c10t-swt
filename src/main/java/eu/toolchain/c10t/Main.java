package eu.toolchain.c10t;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Main {
  public static final String TITLE = "c10t - Graphical Interface";
  
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
        public void onException(final DetachedProcessException e) {

          gui.enableRenderButton();
          gui.updateProgressBar(0);
          gui.updateProgressLabel("Failed to execute command!");
          
          final String message;

          if (e.isDescriptive()) {
            message = e.getMessage();
          } else {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            message = "Got exception while trying to execute command:\n" +
                result.toString();
          }
          
          gui.asyncExec(new Runnable() {
            @Override
            public void run() {
              MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
              messageBox.setMessage(message);
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
        
        stringBuffer.append("Could not find any of the following commands:\n");
        
        for (File f : commandExecutioner.getPaths()) {
          stringBuffer.append("  " + f.getAbsolutePath() + "\n");
        }

        stringBuffer.append("\n");
        
        stringBuffer.append("You must be fixed this by doing one of the following:\n\n" +
                " 1) Install the command `" + commandExecutioner.getName() + "' to somewhere in your PATH or the working directory of this program `" + System.getProperty("user.dir") + "'\n" +
                " 2) Specify where the command is with the environment variable `C10T_PATH'\n\n" +
                "Your PATH is: " + System.getenv("PATH"));

        MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
        messageBox.setText("c10t - Failed to execute command");
        messageBox.setMessage(stringBuffer.toString());
        messageBox.open();
        gui.enableRenderButton();
      }
    }
  }
  
  public static void main (String [] args) {
    Display display = Display.getDefault();
    final Shell shell = new Shell(display, SWT.TITLE | SWT.CLOSE);
    shell.setText(TITLE);

    final String C10T_PATH = System.getenv("C10T_PATH");

    final C10tGraphicalInterface gui = new C10tGraphicalInterface(display, shell);
    final DetachedProcess detachedProcess = new C10tDetachedProcess(gui);
    final CommandExecutioner executioner = new CommandExecutioner("c10t", detachedProcess, C10T_PATH);
    
    gui.addRenderButtonListener(new RenderSelection(shell, gui, executioner));
    
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    gridLayout.marginBottom = 8;
    gridLayout.marginTop = 8;
    gridLayout.marginLeft = 8;
    gridLayout.marginRight = 8;
    
    shell.setLayout (gridLayout);
    shell.pack ();
    shell.open ();
    
    shell.setSize(500, shell.getSize().y);
    
    try {
        executioner.findCommand();
    } catch(CommandNotFoundException e) {
        MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
        messageBox.setText("c10t - Command could not be found");
        messageBox.setMessage("The program `" + executioner.getName() + "' could not be located anywhere in your PATH or in the current working directory\n\n" +
                "You must be fixed this by doing one of the following:\n\n" +
                " 1) Install the command `" + executioner.getName() + "' to somewhere in your PATH or the working directory of this program `" + System.getProperty("user.dir") + "'\n" +
                " 2) Specify where the command is with the environment variable `C10T_PATH'\n\n" +
                "Your PATH is: " + System.getenv("PATH"));
        messageBox.open();
    }

    while (!shell.isDisposed ()) {
      if (!display.readAndDispatch ()) {
    	  display.sleep ();
      }
    }
    
    if (!display.isDisposed()) {
    	display.dispose ();
    }
  }
}
