package com.onehilltech.concurrent;

public interface CompletionCallback
{
  void onFail (Throwable e);
  void onCancel ();
  void onComplete (Object result);
}
