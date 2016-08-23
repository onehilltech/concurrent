package com.onehilltech.concurrent;

class OnComplete implements Runnable
{
  final CompletionCallback callback;
  final Object result;

  OnComplete (CompletionCallback callback, Object result)
  {
    this.callback = callback;
    this.result = result;
  }

  @Override
  public void run ()
  {
    this.callback.onComplete (result);
  }
}
