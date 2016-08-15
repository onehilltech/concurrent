package com.onehilltech.concurrent;

/**
 * @class CompletionCallback
 *
 * Callback used to signal completion of a task.
 */
public abstract class CompletionCallback
{
  /**
   * The task is done with no result.
   */
  public void done ()
  {
    this.done (null);
  }

  /**
   * The task is done and has a result.
   *
   * @param result        Result of the task
   */
  public void done (Object result)
  {
    this.onComplete (result);
  }

  /**
   * The task failed.
   *
   * @param e             Reason for failure
   */
  public void fail (Throwable e)
  {
    this.onFail (e);
  }

  /**
   * Handle notification that the task has failed.
   *
   * @param e
   */
  protected abstract void onFail (Throwable e);

  /**
   * Handle notification that the task, or tasks, was cancelled.
   */
  protected abstract void onCancel ();

  /**
   * Handle notification that the task was completed.
   *
   * @param result
   */
  protected abstract void onComplete (Object result);
}
