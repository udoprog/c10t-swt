package eu.toolchain.c10t;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;

public class CommandExecutioner {
  private List<File> paths;
  private String name;
  private String pathDelimiter = ":";
  
  private DetachedProcess detachedProcess;
  
  public CommandExecutioner(String name, DetachedProcess detachedProcess) {
    this.detachedProcess = detachedProcess;
    
    paths = new ArrayList<File>();
    
    String platform = SWT.getPlatform();
    
    String PATH = System.getenv("PATH");
    String delimiter = ":";
    
    if (platform.equals("win32")) {
      name += ".exe";
      delimiter = ";";
    }
    
    setName(name);
    
    paths.add(new File(".", name));
    
    if (!StringUtils.isEmpty(PATH)) {
      for (String  p : PATH.split(delimiter)) {
        paths.add(new File(p, name));
      }
    }
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public List<File> getPaths() {
    return paths;
  }

  public void setPaths(List<File> paths) {
    this.paths = paths;
  }

  public String getPathDelimiter() {
    return pathDelimiter;
  }

  public void setPathDelimiter(String pathDelimiter) {
    this.pathDelimiter = pathDelimiter;
  }

  public String findCommand() throws CommandNotFoundException {
    List<String> absPaths = new ArrayList<String>();
    
    for (File p : paths) {
      absPaths.add(p.getAbsolutePath());
      
      if (p.isFile() && p.canExecute()) {
        return p.getAbsolutePath();
      }
    }
    
    throw new CommandNotFoundException("Could not find command '" + name + "' as any of the following: " + absPaths);
  }
  
  public Process getProcess(String ... argv) throws CommandNotFoundException {
    String command = findCommand();
    
    List<String> arguments = new ArrayList<String>();
    
    arguments.add(command);
    
    for (String a : argv) {
      arguments.add(a);
    }
    
    System.out.println("Executing command: " + StringUtils.join(arguments, " "));
    
    try {
      return Runtime.getRuntime().exec(arguments.toArray(new String[arguments.size()]));
    } catch(IOException e) {
      throw new CommandNotFoundException(e);
    }
  }
  
  public CommandExecutionThread spawn(DetachedProcessCallback failureCallback, String ... argv)
    throws CommandNotFoundException
  {
    CommandExecutionThread commandExecutionThread =
      new CommandExecutionThread(detachedProcess, failureCallback, getProcess(argv));
    
    commandExecutionThread.start();
    
    return commandExecutionThread;
  }
}
