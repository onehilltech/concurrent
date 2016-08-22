package com.onehilltech.concurrent;

import java.util.concurrent.Executor;

/**
 * Run a task forever. The strategy only returns if the task has been
 * cancelled or there is a failure. Calling onComplete () will just place
 * the task back on the queue for the next iteration.
 */
public class Forever
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  /// The task to execute forever.
  private final Task task_;

  /**
   * Initializing constructor.
   *
   * @param executor        Target executor
   * @param task            Task to execute
   */
  public Forever (Executor executor, Task task)
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
  public Future execute (CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager = new TaskManagerImpl (this.executor_, this.task_, callback);
    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  /**
   * Implementation of the TaskManager for the waterfall
   */
  private class TaskManagerImpl extends TaskManager
  {
    private Task task_;

    private TaskManagerImpl (Executor executor, Task task, CompletionCallback callback)
    {
      super (executor, callback);
      this.task_ = task;
    }

    public boolean isDone ()
    {
      return false;
    }

    @Override
    public void onRun ()
    {
      // Get the current task, and run the task. The task will callback into
      // this task manager when the task completes, or fails. We also catch
      // all exceptions.
      this.task_.run (null, new TaskCompletionCallback (this.task_) {
        @Override
        protected void onComplete (Object result)
        {
          rerunTaskManager ();
        }
      });
    }
  }
}
