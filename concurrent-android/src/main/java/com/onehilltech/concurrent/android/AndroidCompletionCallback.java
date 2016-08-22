package com.onehilltech.concurrent.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.onehilltech.concurrent.CompletionCallback;

/**
 * Specialization of the CompletionCallback that executes its callback methods
 * on the UI thread.
 */
public abstract class AndroidCompletionCallback <T> extends CompletionCallback <T>
{
  private static final int RESULT_COMPLETE = 0;
  private static final int RESULT_CANCEL = 1;
  private static final int RESULT_FAIL = 2;

  /// Message handler
  private final HandlerImpl handler_;

  /**
   * Default constructor that uses the main Looper.
   */
  public AndroidCompletionCallback ()
  {
    this (Looper.getMainLooper ());
  }

  /**
   * Initializing constructor.
   *
   * @param looper        Looper that handles messages
   */
  public AndroidCompletionCallback (Looper looper)
  {
    this.handler_ = new HandlerImpl (looper);
  }

  /**
   * The complete message was posted.
   *
   * @param result      Result, depending on concurrent strategy
   */
  public abstract void onPostComplete (T result);

  /**
   * The cancel message was posted.
   */
  public abstract void onPostCancel ();

  /**
   * The fail message was posted.
   *
   * @param reason         Reason for the failure
   */
  public abstract void onPostFail (Throwable reason);

  @Override
  public final void onCancel ()
  {
    Message msg = this.handler_.obtainMessage (RESULT_CANCEL);
    msg.sendToTarget ();
  }

  @Override
  public final void onFail (Throwable e)
  {
    Message msg = this.handler_.obtainMessage (RESULT_FAIL, e);
    msg.sendToTarget ();
  }

  @Override
  public final void onComplete (Object result)
  {
    Message msg = this.handler_.obtainMessage (RESULT_COMPLETE, result);
    msg.sendToTarget ();
  }

  class HandlerImpl extends Handler
  {
    public HandlerImpl (Looper looper)
    {
      super (looper);
    }

    @Override
    @SuppressWarnings ("unchecked")
    public void handleMessage (Message msg)
    {
      switch (msg.what)
      {
        case RESULT_COMPLETE:
          onPostComplete ((T)msg.obj);
          break;

        case RESULT_CANCEL:
          onPostCancel ();
          break;

        case RESULT_FAIL:
          onPostFail ((Throwable)msg.obj);
          break;
      }
    }
  }
}
