package com.onehilltech.concurrent;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class Race
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
  public Race (Executor executor, Task ... tasks)
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
    private Task firstTaskToComplete_;

    private TaskManagerImpl (Executor executor, Task [] tasks, CompletionCallback callback)
    {
      super (executor, callback);
      this.tasks_ = tasks;
      this.result_ = new HashMap< > ();
    }

    public boolean isDone ()
    {
      return this.firstTaskToComplete_ != null;
    }

    @Override
    public void onRun ()
    {
      // This is a race to the finish. We add all tasks to the queue and
      // only accept the first one to finish. Hopefully, there are enough
      // threads to run all tasks simultaneously. If there is not enough
      // threads, then it is not our concern.

      int index = 0;

      for (Task task : this.tasks_)
        this.executor_.execute (new RaceTask (task, index ++));
    }

    @Override
    public void onComplete (Object result)
    {
      throw new UnsupportedOperationException ();
    }

    private synchronized void onTaskComplete (Task task, int index, Object result)
    {
      // We only accept the first task to complete.
      if (this.firstTaskToComplete_ != null)
        return;

      this.firstTaskToComplete_ = task;

      // Get the name of task, or compute one based on how many tasks have
      // already finished.
      String taskName = task.getName ();

      if (taskName == null)
        taskName = Integer.toString (index);

      this.result_.put (taskName, result);

      // We are done, let's go home.
      this.done ();
    }

    class RaceTask implements Runnable, CompletionCallback
    {
      private final Task task_;
      private final int index_;

      RaceTask (Task task, int index)
      {
        this.task_ = task;
        this.index_ = index;
      }

      @Override
      public void run ()
      {
        try
        {
          if (canContinue ())
            this.task_.run (null, this);
        }
        catch (Exception e)
        {
          fail (e);
        }
      }

      @Override
      public void onCancel ()
      {
        Race.TaskManagerImpl.this.onCancel ();
      }

      @Override
      public void onFail (Throwable e)
      {
        Race.TaskManagerImpl.this.onFail (e);
      }

      @Override
      public void onComplete (Object result)
      {
        onTaskComplete (this.task_, this.index_, result);
      }
    }
  }
}
