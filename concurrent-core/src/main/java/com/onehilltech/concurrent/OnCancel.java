package com.onehilltech.concurrent;

class OnCancel implements Runnable
{
  final CompletionCallback callback;

  OnCancel (CompletionCallback callback)
  {
    this.callback = callback;
  }

  @Override
  public void run ()
  {
    this.callback.onCancel ();
  }
}
