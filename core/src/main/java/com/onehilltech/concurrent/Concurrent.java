package com.onehilltech.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Concurrent
{
  /// Target executor object.
  private Executor executor_;

  /// Singleton instance.
  private static Concurrent instance_;

  /**
   * Get the default instance of the Concurrent.
   *
   * @return
   */
  public static Concurrent getInstance ()
  {
    if (instance_ != null)
      return instance_;

    instance_ = new Concurrent ();
    return instance_;
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

  public DoUntil doUntil (Conditional conditional, Task task)
  {
    return new DoUntil (this.executor_, conditional, task);
  }

  public Forever forever (Task task)
  {
    return new Forever (this.executor_, task);
  }

  public Parallel parallel (Task ... tasks)
  {
    return new Parallel (this.executor_, tasks);
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

  public Until until (Conditional conditional, Task task)
  {
    return new Until (this.executor_, conditional, task);
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

  public <T> ForEach <T> forEach (Task <T> task)
  {
    return new ForEach<> (this.executor_, task);
  }

  /// @}
}
