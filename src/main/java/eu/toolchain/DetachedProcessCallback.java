package eu.toolchain;

public interface DetachedProcessCallback {
	public void onException(DetachedProcessException t);
	public void onSuccess();
}
