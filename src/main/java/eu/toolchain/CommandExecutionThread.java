package eu.toolchain;

public class CommandExecutionThread extends Thread {
	private DetachedProcess detachedThread;
	private DetachedProcessCallback callback;
	private Process p;
	
	public CommandExecutionThread(DetachedProcess detachedThread, DetachedProcessCallback callback, Process p) {
		super();
		this.p = p;
		this.callback = callback;
		this.detachedThread = detachedThread;
	}

	@Override
	public void run() {
		try {
			this.detachedThread.run(p);
			callback.onSuccess();
		} catch(DetachedProcessException e) {
			callback.onException(e);
		}
	}
}
