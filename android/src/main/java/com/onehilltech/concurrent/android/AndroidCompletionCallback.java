package com.onehilltech.concurrent.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.onehilltech.concurrent.CompletionCallback;

/**
 * Specialization of the CompletionCallback that executes its callback methods
 * on the UI thread.
 */
public abstract class AndroidCompletionCallback extends CompletionCallback
{
  private static final int RESULT_COMPLETE = 0;
  private static final int RESULT_CANCEL = 1;
  private static final int RESULT_FAIL = 2;

  private final HandlerImpl handler_;

  public AndroidCompletionCallback ()
  {
    this (Looper.getMainLooper ());
  }

  public AndroidCompletionCallback (Looper looper)
  {
    this.handler_ = new HandlerImpl (looper);
  }

  /**
   * The task are complete
   *
   * @param result      Result, depending on concurrent strategy
   */
  public abstract void onMainComplete (Object result);

  /**
   * The task was cancelled.
   */
  public abstract void onMainCancel ();

  /**
   * The task failed.
   *
   * @param reason         Reason for the failure
   */
  public abstract void onMainFail (Throwable reason);

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
    public void handleMessage (Message msg)
    {
      switch (msg.what)
      {
        case RESULT_COMPLETE:
          onMainComplete (msg.obj);
          break;

        case RESULT_CANCEL:
          onMainCancel ();
          break;

        case RESULT_FAIL:
          onMainFail ((Throwable)msg.obj);
          break;
      }
    }
  }
}
