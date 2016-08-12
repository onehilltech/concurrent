package com.onehilltech.concurrent;

import java.util.concurrent.Executor;

public class Waterfall
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
  public Waterfall (Executor executor, Task ... tasks)
  {
    this.executor_ = executor;
    this.tasks_ = tasks;
  }

  /**
   * Execute the waterfall.
   *
   * @param seed              Value to provide first task
   * @param callback          Callback for the task
   * @return                  Future for managing tasks
   */
  public Future execute (Object seed, CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager = new TaskManagerImpl (this.executor_, this.tasks_, seed, callback);
    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  /**
   * Execute the waterfall.
   *
   * @param callback          Callback for the task
   * @return
   */
  public Future execute (CompletionCallback callback)
  {
    return this.execute (null, callback);
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager
  {
    private int currentTask_ = 0;
    private Task [] tasks_;

    private TaskManagerImpl (Executor executor, Task [] tasks, Object seed, CompletionCallback callback)
    {
      super (executor, callback);
      this.tasks_ = tasks;
      this.result_ = seed;
    }

    public boolean isDone ()
    {
      return this.currentTask_ >= this.tasks_.length;
    }

    @Override
    public void onRun ()
    {
      // Get the current task, and run the task. The task will callback into
      // this task manager when the task completes, or fails. We also catch
      // all exceptions.
      Task task = this.tasks_[this.currentTask_];
      task.run (this.result_, this);
    }

    @Override
    public void onComplete (Object result)
    {
      // Store the result of the task as the last result.
      this.result_ = result;

      // Increment to the next task, and execute the task manager again.
      ++ this.currentTask_;

      this.executor_.execute (this);
    }
  }
}
