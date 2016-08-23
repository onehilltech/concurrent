package com.onehilltech.concurrent;

/**
 * @class CompletionCallback
 *
 * Callback used to signal completion of a task.
 */
public abstract class CompletionCallback <T>
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
  public void done (T result)
  {
    this.onComplete (result);
  }

  /**
   * The task failed.
   *
   * @param reason        Reason for failure
   */
  public void fail (Throwable reason)
  {
    this.onFail (reason);
  }

  /**
   * Handle notification that the task has failed.
   *
   * @param reason        Reason for failure
   */
  protected abstract void onFail (Throwable reason);

  /**
   * Handle notification that the task, or tasks, was cancelled.
   */
  protected abstract void onCancel ();

  /**
   * Handle notification that the task was completed.
   *
   * @param result
   */
  protected abstract void onComplete (T result);
}
