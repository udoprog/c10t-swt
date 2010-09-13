package eu.toolchain;

public class DetachedProcessException extends Exception {
	public DetachedProcessException() {
		super();
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
}
