package com.onehilltech.concurrent;

/**
 * Proxy class that allows you to alter the behavior of an existing CompletionCallback
 * object. Any method not overloaded is passed through to the original CompletionCallback
 * object.
 *
 * The proxy also has the option of passing control to the original object.
 */
public class CompletionCallbackProxy extends CompletionCallback
{
  /// Target completion callback.
  private final CompletionCallback callback_;

  /**
   * Initializing constructor.
   *
   * @param callback        Target callback
   */
  public CompletionCallbackProxy (CompletionCallback callback)
  {
    this.callback_ = callback;
  }

  @Override
  public void onCancel ()
  {
    this.callback_.onCancel ();
  }

  @Override
  public void onFail (Throwable e)
  {
    this.callback_.onFail (e);
  }

  @Override
  public void onComplete (Object o)
  {
    this.callback_.onComplete (o);
  }
}
