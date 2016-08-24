package com.onehilltech.concurrent;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Executor;

/**
 * @class PriorityQueue
 *
 * Queue that orders and executes tasks based on a priority value.
 */
public class PriorityQueue extends Queue
{
  public static final PriorityComparator ASCENDING_PRIORITY = new PriorityComparator ()
  {
    @Override
    protected int compare (int p1, int p2)
    {
      return p1 - p2;
    }
  };

  public static final PriorityComparator DESCENDING_PRIORITY = new PriorityComparator ()
  {
    @Override
    protected int compare (int p1, int p2)
    {
      return p2 - p1;
    }
  };

  public static abstract class PriorityComparator implements Comparator<PendingTask>
  {
    @Override
    public int compare (PendingTask p1, PendingTask p2)
    {
      PriorityPendingTask pp1 = (PriorityPendingTask)p1;
      PriorityPendingTask pp2 = (PriorityPendingTask)p2;

      return this.compare (pp1.getPriority (), pp2.getPriority ());
    }

    protected abstract int compare (int p1, int p2);
  }

  /// Default priority for adding tasks without specifying a priority.
  private int defaultPriority_;

  /**
   * Initialize the queue with a max concurrency value.
   *
   * @param executor          Executor to run tasks
   * @param concurrency       Max concurrency value for tasks
   */
  public PriorityQueue (Executor executor, int concurrency)
  {
    this (executor, concurrency, ASCENDING_PRIORITY);
  }

  /**
   * Initialize the queue with a custom comparator.
   *
   * @param executor          Executor to run tasks
   * @param concurrency       Max concurrency value for tasks
   * @param comparator        Comparator for ordering priorities
   */
  public PriorityQueue (Executor executor, int concurrency, PriorityComparator comparator)
  {
    this (executor, concurrency, comparator, 0);
  }

  /**
   * Initialize the queue with a custom comparator and default priority.
   *
   * @param executor          Executor to run tasks
   * @param concurrency       Max concurrency value for tasks
   * @param comparator        Comparator for ordering priorities
   * @param defaultPriority   Default priority for tasks
   */
  public PriorityQueue (Executor executor, int concurrency, PriorityComparator comparator, int defaultPriority)
  {
    super (new java.util.PriorityQueue <> (concurrency, comparator), executor, concurrency);

    if (defaultPriority < 0)
      throw new IllegalArgumentException ("Default priority cannot be negative");

    this.defaultPriority_ = defaultPriority;
  }

  /**
   * Push a new task onto the queue.
   *
   * @param priority      Priority of tasks
   * @param task          Task to enqueue
   * @param callback      Callback associated with task
   */
  public void push (int priority, Task task, CompletionCallback callback)
  {
    this.push (new PriorityPendingTask (priority, task, callback));
  }

  @Override
  public void push (Task task, CompletionCallback callback)
  {
    this.push (this.defaultPriority_, task, callback);
  }

  @Override
  public void push (Collection<Task> tasks, CompletionCallback callback)
  {
    for (Task task: tasks)
      this.push (this.defaultPriority_, task, callback);
  }

  /**
   * Get the default priority of the queue.
   *
   * @return      The default priority
   */
  public int getDefaultPriority ()
  {
    return this.defaultPriority_;
  }

  /**
   * Set the default priority of the queue.
   *
   * @param defaultPriority       New default priority
   */
  public void setDefaultPriority (int defaultPriority)
  {
    this.defaultPriority_ = defaultPriority;
  }

  class PriorityPendingTask extends PendingTask
  {
    private int priority_;

    public PriorityPendingTask (int priority, Task task, CompletionCallback callback)
    {
      super (task, callback);
      this.priority_ = priority;
    }

    public int getPriority ()
    {
      return this.priority_;
    }
  }
}
