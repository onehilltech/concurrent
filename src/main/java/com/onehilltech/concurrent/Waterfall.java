package com.onehilltech.concurrent;

import java.util.Objects;
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
   * Callback for completing tasks in waterfall.
   */
  public interface CompletionCallback extends BaseCallback
  {
    /**
     * The waterfall execution is complete.
     *
     * @param result      Result from last task in waterfall
     */
    void onComplete (Object result);
  }

  /**
   * Callback used by task to signal completion.
   */
  public interface TaskCallback
  {
    /**
     * Mark the task as done.
     *
     * @param result      Result passed to next Task, or CompletionCallback
     */
    void done (Object result);

    /**
     * Mark the task a failed.
     *
     * @param e
     */
    void fail (Exception e);
  }

  /**
   * Interface for the tasks executed in the waterfall.
   */
  public interface Task
  {
    /**
     * Entry point for task.
     *
     * @param lastResult      Result from last task, null if first task
     * @param callback        Callback used for completion
     */
    void run (Object lastResult, TaskCallback callback);
  }

  /**
   * Execute the waterfall.
   *
   * @param seed              Value to provide first task
   * @param callback          Callback for the task
   * @return
   */
  public Future execute (Object seed, CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManager taskManager = new TaskManager (this.tasks_, seed, callback);
    this.executor_.execute (taskManager);

    return new FutureImpl (taskManager);
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

  private class TaskManager
      implements Runnable, TaskCallback
  {
    private int currentTask_ = 0;
    private Task [] tasks_;
    private CompletionCallback completionCallback_;

    private Object lastResult_;
    private boolean isCancelled_ = false;
    private Exception failure_;

    private TaskManager (Task [] tasks, Object seed, CompletionCallback callback)
    {
      this.tasks_ = tasks;
      this.completionCallback_ = callback;
      this.lastResult_ = seed;
    }

    boolean isDone ()
    {
      return this.currentTask_ >= this.tasks_.length;
    }

    void cancel ()
    {
      this.isCancelled_ = true;
    }

    @Override
    public void run ()
    {
      if (this.failure_ != null)
      {
        this.completionCallback_.onFail (this.failure_);
      }
      else if (this.isCancelled_)
      {
        this.completionCallback_.onCancel ();
      }
      else if (this.isDone ())
      {
        this.completionCallback_.onComplete (this.lastResult_);
      }
      else
      {
        try
        {
          // Get the current task, and run the task. The task will callback into
          // this task manager when the task completes, or fails. We also catch
          // all exceptions.
          Task task = this.tasks_[this.currentTask_++];
          task.run (this.lastResult_, this);
        }
        catch (Exception e)
        {
          this.failure_ = e;
          executor_.execute (this);
        }
      }
    }

    @Override
    public void done (Object result)
    {
      // The current task is complete, save the result for the next task or
      // passing back the client. We then need to execute the task manager
      // again.
      this.lastResult_ = result;
      executor_.execute (this);
    }

    @Override
    public void fail (Exception reason)
    {
      this.failure_ = reason;
      executor_.execute (this);
    }
  }

  private class FutureImpl implements Future
  {
    private final TaskManager taskManager_;

    FutureImpl (TaskManager taskManager)
    {
      this.taskManager_ = taskManager;
    }

    @Override
    public boolean isDone ()
    {
      return this.taskManager_.isDone ();
    }

    @Override
    public void cancel ()
    {
      this.taskManager_.cancel ();
    }
  }
}
