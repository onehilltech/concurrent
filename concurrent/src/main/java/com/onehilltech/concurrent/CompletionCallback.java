package com.onehilltech.concurrent;

public abstract class CompletionCallback <T>
{
  public final void done (T result)
  {
    this.onComplete (result);
  }

  public final void fail (Throwable e)
  {
    this.onFail (e);
  }

  protected abstract void onFail (Throwable e);
  protected abstract void onCancel ();
  protected abstract void onComplete (T result);
}
