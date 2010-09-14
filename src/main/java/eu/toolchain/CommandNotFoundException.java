package eu.toolchain;

public class CommandNotFoundException extends Exception {
  public CommandNotFoundException() {
    super();
  }
  
  public CommandNotFoundException(String reason) {
    super(reason);
  }
  
  public CommandNotFoundException(String reason, Throwable t) {
    super(reason, t);
  }
  
  public CommandNotFoundException(Throwable t) {
    super(t);
  }
}
