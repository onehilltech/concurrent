package com.onehilltech.concurrent;

class OnFail implements Runnable
{
  private final CompletionCallback callback_;
  private final Throwable failure_;

  OnFail (CompletionCallback callback, Throwable failure)
  {
    this.callback_ = callback;
    this.failure_ = failure;
  }

  @Override
  public void run ()
  {
    this.callback_.onFail (this.failure_);
  }
}
