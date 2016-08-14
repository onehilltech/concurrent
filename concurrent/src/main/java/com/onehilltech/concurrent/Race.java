package com.onehilltech.concurrent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private List<Task> tasks_;
    private Task firstTaskToComplete_;

    private TaskManagerImpl (Executor executor, Task [] tasks, CompletionCallback callback)
    {
      super (executor, callback);
      this.tasks_ = Arrays.asList (tasks);
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
      for (Task task : this.tasks_)
        this.executor_.execute (new RaceTask (task));
    }

    @Override
    public void onTaskComplete (Task task, Object result)
    {
      // We only accept the first task to complete.
      if (this.firstTaskToComplete_ != null)
        return;

      this.firstTaskToComplete_ = task;

      // Get the name of task, or compute one based on how many tasks have
      // already finished.
      String taskName = task.getName ();

      if (taskName == null)
      {
        int index = this.tasks_.indexOf (task);

        if (index == -1)
          throw new IllegalArgumentException ("Invalid task");

        taskName = Integer.toString (index);
      }

      this.result_.put (taskName, result);

      // We are done, let's go home.
      this.done ();
    }



    class RaceTask implements Runnable
    {
      private final Task task_;

      RaceTask (Task task)
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
