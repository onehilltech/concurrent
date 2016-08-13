package com.onehilltech.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Concurrent
{
  private Executor executor_;

  private static Concurrent instance_;

  public static Concurrent getInstance ()
  {
    if (instance_ != null)
      return instance_;

    instance_ = new Concurrent ();
    return instance_;
  }

  public Concurrent ()
  {
    this (Executors.newCachedThreadPool ());
  }

  public Concurrent (Executor executor)
  {
    this.executor_ = executor;
  }

  public Executor getExecutor ()
  {
    return this.executor_;
  }

  /// @{ Control Flow

  public Series series (Task ... tasks) { return new Series (this.executor_, tasks); }
  public Times times (Task task) { return new Times (this.executor_, task); }
  public Waterfall waterfall (Task ... tasks)
  {
    return new Waterfall (this.executor_, tasks);
  }

  /// @}

  /// @{ Collections

  public <T> ForEach <T> forEach (Task <T> task)
  {
    return new ForEach<T> (this.executor_, task);
  }

  /// @}
}
