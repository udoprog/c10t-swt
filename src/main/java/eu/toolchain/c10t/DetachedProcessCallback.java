package eu.toolchain.c10t;

public interface DetachedProcessCallback {
  public void onException(DetachedProcessException t);
  public void onSuccess();
}
