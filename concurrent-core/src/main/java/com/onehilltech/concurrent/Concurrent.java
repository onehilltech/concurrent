package com.onehilltech.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Concurrent
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

  public DoUntil doUntil (Conditional cond, Task task)
  {
    return new DoUntil (this.executor_, cond, task);
  }

  public DoWhile doWhile (Conditional cond, Task task)
  {
    return new DoWhile (this.executor_, cond, task);
  }

  public During during (ConditionalTask cond, Task task)
  {
    return new During (this.executor_, cond, task);
  }

  public Forever forever (Task task)
  {
    return new Forever (this.executor_, task);
  }

  public Parallel parallel (Task ... tasks)
  {
    return new Parallel (this.executor_, tasks);
  }

  public Queue queue (int concurrency)
  {
    return new Queue (this.executor_, concurrency);
  }

  public Race race (Task ... tasks)
  {
    return new Race (this.executor_, tasks);
  }

  public Retry retry (Task task)
  {
    return new Retry (this.executor_, task);
  }

  public Series series (Task ... tasks)
  {
    return new Series (this.executor_, tasks);
  }

  public Times times (Task task)
  {
    return new Times (this.executor_, task);
  }

  public Until until (Conditional cond, Task task)
  {
    return new Until (this.executor_, cond, task);
  }

  public Waterfall waterfall (Task ... tasks)
  {
    return new Waterfall (this.executor_, tasks);
  }

  public While whilst (Conditional cond, Task task)
  {
    return new While (this.executor_, cond, task);
  }

  /// @}

  /// @{ Collections

  public <T> Every <T> every (Task <T> task)
  {
    return new Every<> (this.executor_, task);
  }

  public <T> ForEach <T> forEach (Task <T> task)
  {
    return new ForEach<> (this.executor_, task);
  }

  public <T> Some <T> some (Task <T> task)
  {
    return new Some <> (this.executor_, task);
  }

  /// @}
}
