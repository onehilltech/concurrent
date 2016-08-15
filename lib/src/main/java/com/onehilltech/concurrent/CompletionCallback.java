package com.onehilltech.concurrent;

public abstract class CompletionCallback
{
  public final void done (Object result)
  {
    this.onComplete (result);
  }

  public final void fail (Throwable e)
  {
    this.onFail (e);
  }

  protected abstract void onFail (Throwable e);
  protected abstract void onCancel ();
  protected abstract void onComplete (Object result);
}
