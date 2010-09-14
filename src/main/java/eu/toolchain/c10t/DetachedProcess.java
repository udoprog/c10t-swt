package eu.toolchain.c10t;

public interface DetachedProcess {
  public void run(Process p) throws DetachedProcessException;
}
