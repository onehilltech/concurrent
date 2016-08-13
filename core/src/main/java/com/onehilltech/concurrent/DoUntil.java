package com.onehilltech.concurrent;

import java.util.concurrent.Executor;

/**
 * Execute a task, and retry the task if it fails up to N times. There is an
 * optional interval for between retries that is only applicable when using and
 * instance of ScheduledExecutorService executor.
 */
public class DoUntil
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  /// The task to execute N times.
  private final Task task_;

  private final Conditional conditional_;

  /**
   * Initializing constructor.
   *
   * @param executor        Target executor
   * @param task            Task to execute
   */
  public DoUntil (Executor executor, Conditional conditional, Task task)
  {
    this.executor_ = executor;
    this.conditional_ = conditional;
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
            this.conditional_,
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
    private final Conditional conditional_;
    private final Task task_;

    private TaskManagerImpl (Executor executor,
                             Conditional conditional,
                             Task task,
                             CompletionCallback callback)
    {
      super (executor, callback);
      this.conditional_ = conditional;
      this.task_ = task;
    }

    @Override
    public boolean isDone ()
    {
      return this.conditional_.evaluate ();
    }

    @Override
    public void onRun ()
    {
      // Run the task since the condition is still true.
      this.task_.run (null, this);
    }

    @Override
    public void onComplete (Object result)
    {
      this.result_ = result;
      this.executor_.execute (this);
    }
  }
}
