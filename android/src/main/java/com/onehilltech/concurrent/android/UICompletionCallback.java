package com.onehilltech.concurrent.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.onehilltech.concurrent.CompletionCallback;

/**
 * Specialization of the CompletionCallback that executes its callback methods
 * on the UI thread.
 */
public abstract class UICompletionCallback implements CompletionCallback
{
  private static final int RESULT_COMPLETE = 0;
  private static final int RESULT_CANCEL = 1;
  private static final int RESULT_FAIL = 2;

  private final Handler handler_ = new Handler (Looper.getMainLooper ()) {
    @Override
    public void handleMessage (Message msg)
    {
      switch (msg.what)
      {
        case RESULT_COMPLETE:
          onUIComplete (msg.obj);
          break;

        case RESULT_CANCEL:
          onUICancel ();
          break;

        case RESULT_FAIL:
          onUIFail ((Throwable)msg.obj);
          break;
      }
    }
  };

  /**
   * The task are complete
   *
   * @param result      Result, depending on concurrent strategy
   */
  public abstract void onUIComplete (Object result);

  /**
   * The task was cancelled.
   */
  public abstract void onUICancel ();

  /**
   * The task failed.
   *
   * @param reason         Reason for the failure
   */
  public abstract void onUIFail (Throwable reason);

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
}
