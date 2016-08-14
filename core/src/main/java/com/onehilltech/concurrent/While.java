package com.onehilltech.concurrent;

import java.util.concurrent.Executor;

/**
 * Execute a task, and retry the task if it fails up to N times. There is an
 * optional interval for between retries that is only applicable when using and
 * instance of ScheduledExecutorService executor.
 */
public class While
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  /// The task to execute N times.
  private final Task task_;

  /// Conditional controlling the loop
  private final Conditional cond_;

  /**
   * Initializing constructor.
   *
   * @param executor        Target executor
   * @param cond            Conditional controlling the loop
   * @param task            Task to execute
   */
  public While (Executor executor, Conditional cond, Task task)
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
    private final Conditional cond_;
    private final Task task_;

    private TaskManagerImpl (Executor executor,
                             Conditional cond,
                             Task task,
                             CompletionCallback callback)
    {
      super (executor, callback);
      this.cond_ = cond;
      this.task_ = task;
    }

    @Override
    public boolean isDone ()
    {
      return !this.cond_.evaluate ();
    }

    @Override
    public void onRun ()
    {
      // Run the task since the condition is still true.
      this.task_.run (null, new TaskCompletionCallback (this.task_));
    }

    @Override
    public void onTaskComplete (Task task, Object result)
    {
      this.result_ = result;
      this.executor_.execute (this);
    }
  }
}
