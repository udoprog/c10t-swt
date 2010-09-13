package eu.toolchain;

public interface DetachedProcess {
	public void run(Process p) throws DetachedProcessException;
}
