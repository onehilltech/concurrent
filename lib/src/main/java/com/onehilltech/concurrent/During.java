package com.onehilltech.concurrent;

import java.util.concurrent.Executor;

/**
 * Execute a task, and retry the task if it fails up to N times. There is an
 * optional interval for between retries that is only applicable when using and
 * instance of ScheduledExecutorService executor.
 */
public class During
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  /// The task to execute N times.
  private final Task task_;

  /// Conditional controlling the loop
  private final ConditionalTask cond_;

  /**
   * Initializing constructor.
   *
   * @param executor        Target executor
   * @param cond            Conditional controlling the loop
   * @param task            Task to execute
   */
  public During (Executor executor, ConditionalTask cond, Task task)
  {
    this.executor_ = executor;
    this.cond_ = cond;
    this.task_ = task;
  }

  /**
   * Execute the task with N retries, and an interval between retries.
   *
   * @param callback          Callback for the task
   * @return
   */
  public Future execute (CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager =
        new TaskManagerImpl (
            this.executor_,
            this.cond_,
            this.task_,
            callback);

    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager
  {
    private final ConditionalTask condTask_;
    private final Task task_;

    private boolean cond_ = true;

    /**
     * Conditional callback that determines if iteration is complete.
     */
    private final ConditionalCallback conditionalCallback_ = new ConditionalCallback ()
    {
      @Override
      protected void onFail (Throwable e)
      {
        TaskManagerImpl.this.fail (e);
      }

      @Override
      protected void onCancel ()
      {
        TaskManagerImpl.this.cancel ();
      }

      @Override
      protected void onComplete (Object result)
      {
        onConditionComplete ((boolean)result);
      }
    };

    /**
     * Handle completion of the conditional task.
     *
     * @param result
     */
    private void onConditionComplete (boolean result)
    {
      // Store the conditional result.
      this.cond_ = result;

      if (this.isDone ())
        this.done ();
      else
        this.executor_.execute (new TaskRunner ());
    }

    class TaskRunner implements Runnable
    {
      @Override
      public void run ()
      {
        task_.run (null, new TaskCompletionCallback (task_));
      }
    }

    private TaskManagerImpl (Executor executor,
                             ConditionalTask cond,
                             Task task,
                             CompletionCallback callback)
    {
      super (executor, callback);

      this.condTask_ = cond;
      this.task_ = task;
    }

    @Override
    public boolean isDone ()
    {
      return !this.cond_;
    }

    @Override
    public void onRun ()
    {
      this.condTask_.evaluate (this.conditionalCallback_);
    }

    @Override
    public void onTaskComplete (Task task, Object result)
    {
      this.result_ = result;
      this.executor_.execute (this);
    }
  }
}
