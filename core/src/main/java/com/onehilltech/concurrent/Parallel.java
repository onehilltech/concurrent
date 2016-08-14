package com.onehilltech.concurrent;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class Parallel
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
  public Parallel (Executor executor, Task ... tasks)
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
    private Task [] tasks_;
    private final Object completeLock_ = new Object ();

    private TaskManagerImpl (Executor executor, Task [] tasks, CompletionCallback callback)
    {
      super (executor, callback);
      this.tasks_ = tasks;
      this.result_ = new HashMap< > ();
    }

    public boolean isDone ()
    {
      return this.result_.size () == this.tasks_.length;
    }

    @Override
    public void onRun ()
    {
      for (Task task : this.tasks_)
        this.executor_.execute (new ParallelTask (task));
    }

    @Override
    public void onTaskComplete (Task task, Object result)
    {
      // Get the name of task, or compute one based on how many tasks have
      // already finished.
      String taskName = task.getName ();

      if (taskName == null)
        taskName = Integer.toString (this.result_.size ());

      synchronized (this.completeLock_)
      {
        this.result_.put (taskName, result);
      }

      if (this.isDone ())
        this.done ();
    }

    private class ParallelTask implements Runnable
    {
      private final Task task_;

      public ParallelTask (Task task)
      {
        this.task_ = task;
      }

      @Override
      public void run ()
      {
        try
        {
          if (canContinue ())
            this.task_.run (null, new TaskCompletionCallback (this.task_));
        }
        catch (Exception e)
        {
          fail (e);
        }
      }
    }
  }
}
