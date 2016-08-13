package com.onehilltech.concurrent;

import java.util.concurrent.Executor;

/**
 * Execute a task, and retry the task if it fails up to N times. There is an
 * optional interval for between retries that is only applicable when using and
 * instance of ScheduledExecutorService executor.
 */
public class DoWhile
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
   * @param executor Target executor
   * @param cond     Conditional controlling the loop
   * @param task     Task to execute
   */
  public DoWhile (Executor executor, Conditional cond, Task task)
  {
    this.executor_ = executor;
    this.cond_ = cond;
    this.task_ = task;
  }

  /**
   * Execute the task with N retries, and an interval between retries.
   *
   * @param callback Callback for the task
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

  private interface Iteration
  {
    boolean evaluate ();
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager
  {
    private final Conditional cond_;

    private final Task task_;

    private Iteration iteration_ = new FirstIteration ();

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
      return !this.iteration_.evaluate ();
    }

    @Override
    public void onRun ()
    {
      this.task_.run (null, this);
    }

    @Override
    public void onComplete (Object result)
    {
      this.result_ = result;
      this.executor_.execute (this);
    }

    private class FirstIteration implements Iteration
    {
      @Override
      public boolean evaluate ()
      {
        // Replace the iteration with the state for the remaining iterations.
        iteration_ = new RemainingIterations ();

        // We always return true for the first iteration. This will ensure the
        // task executes at least once.
        return true;
      }
    }

    private class RemainingIterations implements Iteration
    {
      @Override
      public boolean evaluate ()
      {
        return cond_.evaluate ();
      }
    }
  }
}
