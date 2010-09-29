package eu.toolchain.c10t;

public class DetachedProcessException extends Exception {
  private boolean descriptive = false;
  
  public DetachedProcessException() {
    super();
  }
  
  public DetachedProcessException(String reason, boolean descriptive) {
    super(reason);
    this.descriptive = descriptive;
  }
  
  public DetachedProcessException(String reason) {
    super(reason);
  }
  
  public DetachedProcessException(String reason, Throwable t) {
    super(reason, t);
  }
  
  public DetachedProcessException(Throwable t) {
    super(t);
  }
  
  public boolean isDescriptive() {
    return descriptive;
  }
}
