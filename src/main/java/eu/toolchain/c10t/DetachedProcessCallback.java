package eu.toolchain.c10t;

public interface DetachedProcessCallback {
  public void onException(final DetachedProcessException t);
  public void onSuccess();
}
