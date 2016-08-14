package com.onehilltech.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;

public class Some <T>
{
  /// Target executor that executes the tasks.
  private final Executor executor_;

  private Task <T> task_;

  /**
   * Initializing constructor.
   *
   * @param executor Executor for running iterator
   * @param task     Task to run on each element in the collection
   */
  public Some (Executor executor, Task<T> task)
  {
    this.executor_ = executor;
    this.task_ = task;
  }

  /**
   * Execute the waterfall.
   *
   * @param callback Callback for the task
   * @return
   */
  public Future execute (Collection <T> coll, CompletionCallback callback)
  {
    if (callback == null)
      throw new IllegalArgumentException ("Callback cannot be null");

    TaskManagerImpl taskManager = new TaskManagerImpl (this.executor_, this.task_, coll, callback);
    this.executor_.execute (taskManager);

    return new Future (taskManager);
  }

  private class TaskManagerImpl extends TaskManager <Boolean>
  {
    private Task<T> task_;
    private Iterator <T> iter_;

    private TaskManagerImpl (Executor executor,
                             Task<T> task,
                             Collection <T> coll,
                             CompletionCallback callback)
    {
      super (executor, callback);

      this.result_ = false;
      this.iter_ = coll.iterator ();
      this.task_ = task;
    }

    @Override
    protected boolean isDone ()
    {
      return this.result_ || !this.iter_.hasNext ();
    }

    @Override
    public void onRun ()
    {
      T item = this.iter_.next ();
      this.task_.run (item, new TaskCompletionCallback (this.task_));
    }

    @Override
    public void onTaskComplete (Task task, Object result)
    {
      this.result_ = (Boolean)result;
      this.executor_.execute (this);
    }
  }
}

