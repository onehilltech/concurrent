package com.onehilltech.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Execute a task, and retry the task if it fails up to N times. There is an
 * optional interval for between retries that is only applicable when using and
 * instance of ScheduledExecutorService executor.
 */
public class Retry
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  /// The task to execute N times.
  private final Task task_;

  /**
   * Initializing constructor.
   *
   * @param executor        Target executor
   * @param task            Task to execute
   */
  public Retry (Executor executor, Task task)
  {
    this.executor_ = executor;
    this.task_ = task;
  }

  /**
   * Execute the task with N retries, and an interval between retries.
   *
   * @param retries           Number of retries
   * @param callback          Callback for the task
   * @return                  Future for managing tasks
   */
  public Future execute (int retries, CompletionCallback callback)
  {
    return this.execute (retries, 0, callback);
  }

  /**
   * Execute the task with N retries, and an interval between retries.
   *
   * @param retries           Number of retries
   * @param interval          Number of msec between retries
   * @param callback          Callback for the task
   * @return
   */
  public Future execute (int retries, int interval, CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager =
        new TaskManagerImpl (
            this.executor_,
            this.task_,
            retries,
            interval,
            callback);

    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager
  {
    private int retries_;
    private final int interval_;
    private final Task task_;
    private boolean isDone_ = false;

    private TaskManagerImpl (Executor executor,
                             Task task,
                             int retries,
                             int interval,
                             CompletionCallback callback)
    {
      super (executor, callback);
      this.task_ = task;
      this.retries_ = retries;
      this.interval_ = interval;
    }

    @Override
    public boolean isDone ()
    {
      return this.isDone_;
    }

    /**
     * Get the number of retries remaining.
     *
     * @return
     */
    public int getRemainingCount ()
    {
      return this.retries_;
    }

    @Override
    public void onRun ()
    {
      // Get the current task, and run the task. The task will callback into
      // this task manager when the task completes, or fails. We also catch
      // all exceptions.
      this.task_.run (null, this);
    }

    @Override
    public void onComplete (Object result)
    {
      this.result_ = result;
      this.isDone_ = true;

      this.executor_.execute (this);
    }

    @Override
    public void onFail (Throwable e)
    {
      if (this.retries_ > 0)
      {
        // Decrement the number of retries.
        -- this.retries_;

        if (this.interval_ > 0)
        {
          if ((this.executor_ instanceof ScheduledExecutorService))
          {
            ScheduledExecutorService service = (ScheduledExecutorService)this.executor_;
            service.schedule (this, this.interval_, TimeUnit.MILLISECONDS);
          }
        }
        else
        {
          // Retry the task.
          this.executor_.execute (this);
        }
      }
      else
      {
        super.onFail (e);
      }
    }
  }
}
