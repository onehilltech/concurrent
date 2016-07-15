package com.onehilltech.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ForEach <T>
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
  public ForEach (Executor executor, Task<T> task)
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

  private class TaskManagerImpl extends TaskManager
  {
    private Task<T> task_;
    private Collection<T> coll_;

    /// Keep track of the number of items remaining since we do not guarantee any
    /// order when running the items on the collection. When this count reaches 0,
    /// then we have run the task on each item in the collection.
    private final AtomicInteger remainingCount_;

    private TaskManagerImpl (Executor executor,
                             Task<T> task,
                             Collection <T> coll,
                             CompletionCallback callback)
    {
      super (executor, callback);

      this.coll_ = coll;
      this.remainingCount_ = new AtomicInteger (coll.size ());

      this.task_ = task;
    }

    @Override
    protected boolean isDone ()
    {
      return this.remainingCount_.get () == 0;
    }

    @Override
    public void onRun ()
    {
      // Get the next item from the collection, and run the item on its own thread. This
      // is allowed because we do not care about the order of execution/completion for each
      // item. We just care that the task was run on each item.
      for (T item : this.coll_)
        this.executor_.execute (new ItemTask (item));
    }

    /**
     * Run the item on its own thread.
     */
    private class ItemTask implements Runnable
    {
      private final T item_;

      ItemTask (T item)
      {
        this.item_ = item;
      }

      @Override
      public void run ()
      {
        try
        {
          if (canContinue ())
            task_.run (this.item_);
        }
        catch (Exception e)
        {
          fail (e);
        }
        finally
        {
          int remaining = remainingCount_.decrementAndGet ();

          if (remaining == 0)
            done ();
        }
      }

    }
  }
}

