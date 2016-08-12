package com.onehilltech.concurrent;

public interface CompletionCallback
{
  void onFail (Exception e);
  void onCancel ();
  void onComplete (Object result);
}
