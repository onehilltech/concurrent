package com.onehilltech.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @class Concurrent
 *
 * Wrapper class for creating different concurrent strategies atop the same Executor
 * object.
 */
public final class Concurrent
{
  /// Target executor object.
  private Executor executor_;

  /// Singleton instance.
  private static Concurrent default_;

  /**
   * Get the default instance of the Concurrent.
   *
   * @return
   */
  public static Concurrent getDefault ()
  {
    if (default_ != null)
      return default_;

    default_ = new Concurrent ();
    return default_;
  }

  /**
   * Default constructor.
   */
  public Concurrent ()
  {
    this (Executors.newCachedThreadPool ());
  }

  /**
   * Construct Concurrent on an existing Executor.
   *
   * @param executor        Target executor.
   */
  public Concurrent (Executor executor)
  {
    this.executor_ = executor;
  }

  /**
   * Get the executor object.
   *
   * @return
   */
  public Executor getExecutor ()
  {
    return this.executor_;
  }

  /// @{ Control Flow

  /**
   * Create a DoUntil control flow strategy.
   *
   * @param cond      Looping condition
   * @param task      Task to execute each iteration
   * @return          DoUntil object
   */
  public DoUntil doUntil (Conditional cond, Task task)
  {
    return new DoUntil (this.executor_, cond, task);
  }

  /**
   * Create a DoWhile control flow strategy.
   *
   * @param cond      Looping condition
   * @param task      Task to execute each iteration
   * @return          DoWhile object
   */
  public DoWhile doWhile (Conditional cond, Task task)
  {
    return new DoWhile (this.executor_, cond, task);
  }

  /**
   * Create a During control flow strategy.
   *
   * @param cond      Looping condition
   * @param task      Task to execute each loop
   * @return          During object
   */
  public During during (ConditionalTask cond, Task task)
  {
    return new During (this.executor_, cond, task);
  }

  /**
   * Create a Forever control flow strategy.
   *
   * @param task      Task to execute forever
   * @return          Forever object
   */
  public Forever forever (Task task)
  {
    return new Forever (this.executor_, task);
  }

  /**
   * Create a Parallel control flow strategy.
   *
   * @param tasks     Collection of tasks to execute in parallel
   * @return          Parallel object
   */
  public Parallel parallel (Task ... tasks)
  {
    return new Parallel (this.executor_, tasks);
  }

  /**
   * Create a PriorityQueue control flow strategy.
   *
   * @param concurrency       Concurrency for priority queue
   * @return                  PriorityQueue object
   */
  public PriorityQueue priorityQueue (int concurrency)
  {
    return new PriorityQueue (this.executor_, concurrency);
  }

  /**
   * Create a PriorityQueue control flow strategy.
   *
   * @param concurrency       Concurrency for priority queue
   * @param comparator        Comparator for priorities
   * @return                  PriorityQueue object
   */
  public PriorityQueue priorityQueue (int concurrency,
                                      PriorityQueue.PriorityComparator comparator)
  {
    return new PriorityQueue (this.executor_, concurrency, comparator);
  }

  /**
   * Create a PriorityQueue control flow strategy.
   *
   * @param concurrency       Concurrency for priority queue
   * @param comparator        Comparator for priorities
   * @param defaultPriority   Default priority for unspecified tasks
   * @return                  PriorityQueue object
   */
  public PriorityQueue priorityQueue (int concurrency,
                                      PriorityQueue.PriorityComparator comparator,
                                      int defaultPriority)
  {
    return new PriorityQueue (this.executor_, concurrency, comparator, defaultPriority);
  }

  /**
   * Create a Queue control flow strategy.
   *
   * @param concurrency       Concurrency for queue
   * @return                  Queue object
   */
  public Queue queue (int concurrency)
  {
    return new Queue (this.executor_, concurrency);
  }

  /**
   * Create a Race control flow strategy.
   *
   * @param tasks             Tasks to execute in race
   * @return                  Race object
   */
  public Race race (Task ... tasks)
  {
    return new Race (this.executor_, tasks);
  }

  /**
   * Create a Retry control flow strategy.
   *
   * @param task              Task to execute, and retry if fails
   * @return                  Retry object
   */
  public Retry retry (Task task)
  {
    return new Retry (this.executor_, task);
  }

  /**
   * Create a Series control flow strategy.
   *
   * @param tasks             Collection of tasks to execute
   * @return                  Series object
   */
  public Series series (Task ... tasks)
  {
    return new Series (this.executor_, tasks);
  }

  /**
   * Create a Times control flow strategy.
   *
   * @param task              Tasks to execute a number of times
   * @return                  Times object
   */
  public Times times (Task task)
  {
    return new Times (this.executor_, task);
  }

  /**
   * Create an Until control flow strategy.
   *
   * @param cond              Looping condition
   * @param task              Task to execute each iteration
   * @return                  Until object
   */
  public Until until (Conditional cond, Task task)
  {
    return new Until (this.executor_, cond, task);
  }

  /**
   * Create a Waterfall control flow strategy.
   *
   * @param tasks             Collection of tasks to execute
   * @return                  Waterfall object
   */
  public Waterfall waterfall (Task ... tasks)
  {
    return new Waterfall (this.executor_, tasks);
  }

  /**
   * Create a While control flow strategy.
   *
   * @param cond              Looping condition
   * @param task              Task to execute each iteration
   * @return                  While object
   */
  public While whilst (Conditional cond, Task task)
  {
    return new While (this.executor_, cond, task);
  }

  /// @}

  /// @{ Collections

  /**
   * Create an Every collection strategy.
   *
   * @param task          Task that must pass every item in collection
   * @return              Every object
   */
  public <T> Every <T> every (Task <T> task)
  {
    return new Every<> (this.executor_, task);
  }

  /**
   * Create a ForEach collection strategy.
   *
   * @param task          Task to execute on each item in collection
   * @return              ForEach object
   */
  public <T> ForEach <T> forEach (Task <T> task)
  {
    return new ForEach<> (this.executor_, task);
  }

  /**
   * Create an Some collection strategy.
   *
   * @param task          Task that must pass at least one item in collection
   * @return              Some object
   */
  public <T> Some <T> some (Task <T> task)
  {
    return new Some <> (this.executor_, task);
  }

  /// @}
}
