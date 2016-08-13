package com.onehilltech.concurrent;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class Times
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
  public Times (Executor executor, Task task)
  {
    this.executor_ = executor;
    this.task_ = task;
  }

  /**
   * Execute the series.
   *
   * @param callback          Callback for the task
   * @return                  Future for managing tasks
   */
  public Future execute (int times, CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager = new TaskManagerImpl (this.executor_, this.task_, times, callback);
    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager <ArrayList <Object>>
  {
    private int times_;
    private Task task_;

    private TaskManagerImpl (Executor executor, Task task, int times, CompletionCallback callback)
    {
      super (executor, callback);
      this.task_ = task;
      this.times_ = times;
      this.result_ = new ArrayList<> ();
    }

    public boolean isDone ()
    {
      return this.result_.size () >= this.times_;
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
      // Push the result onto the result set, then run the executor again.
      this.result_.add (result);

      this.executor_.execute (this);
    }
  }
}
