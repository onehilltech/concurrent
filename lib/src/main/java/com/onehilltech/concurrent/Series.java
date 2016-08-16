package com.onehilltech.concurrent;

import java.util.HashMap;
import java.util.Map;
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
  public Future execute (CompletionCallback <Map <String, Object>> callback)
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
  private class TaskManagerImpl extends TaskManager <Map <String, Object>>
  {
    private int current_ = 0;
    private Task [] tasks_;

    private TaskManagerImpl (Executor executor,
                             Task [] tasks,
                             CompletionCallback <Map <String, Object>> callback)
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
    @SuppressWarnings ("unchecked")
    public void onRun ()
    {
      // Get the current task, and run the task. The task will callback into
      // this task manager when the task completes, or fails. We also catch
      // all exceptions.
      Task task = this.tasks_[this.current_];

      task.run (null, new TaskCompletionCallback <Object> (task) {
        @Override
        protected void onComplete (Object result)
        {
          // Store the result either under the name of the task, or the index of the task.
          String taskName = this.task_.getName ();

          if (taskName == null)
            taskName = Integer.toString (current_);

          result_.put (taskName, result);

          // Increment the current task, and then run the executor again.
          ++ current_;

          rerunTaskManager ();
        }
      });
    }
  }
}
