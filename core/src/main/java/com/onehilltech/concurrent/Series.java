package com.onehilltech.concurrent;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class Series
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  /// Collection of tasks to execute.
  private final Task [] tasks_;

  /**
   * Initializing constructor.
   *
   * @param executor
   * @param tasks
   */
  public Series (Executor executor, Task ... tasks)
  {
    this.executor_ = executor;
    this.tasks_ = tasks;
  }

  /**
   * Execute the series.
   *
   * @param callback          Callback for the task
   * @return                  Future for managing tasks
   */
  public Future execute (CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager = new TaskManagerImpl (this.executor_, this.tasks_, callback);
    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager <HashMap <String, Object>>
  {
    private int current_ = 0;
    private Task [] tasks_;
    private Task currentTask_;

    private TaskManagerImpl (Executor executor, Task [] tasks, CompletionCallback callback)
    {
      super (executor, callback);
      this.tasks_ = tasks;
      this.result_ = new HashMap< > ();
    }

    public boolean isDone ()
    {
      return this.current_ >= this.tasks_.length;
    }

    @Override
    public void onRun ()
    {
      // Get the current task, and run the task. The task will callback into
      // this task manager when the task completes, or fails. We also catch
      // all exceptions.
      this.currentTask_ = this.tasks_[this.current_];
      this.currentTask_.run (null, this);
    }

    @Override
    public void onComplete (Object result)
    {
      // Store the result either under the name of the task, or the index of the task.
      String taskName = this.currentTask_.getName ();

      if (taskName == null)
        taskName = Integer.toString (this.current_);

      this.result_.put (taskName, result);

      // Increment the current task, and then run the executor again.
      ++ this.current_;

      this.executor_.execute (this);
    }
  }
}
