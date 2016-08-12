package com.onehilltech.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Concurrent
{
  private Executor executor_;

  public Concurrent ()
  {
    this (Executors.newCachedThreadPool ());
  }

  public Concurrent (Executor executor)
  {
    this.executor_ = executor;
  }

  /// @{ Control Flow

  public Waterfall waterfall (Task ... tasks)
  {
    return new Waterfall (this.executor_, tasks);
  }
  public Series series (Task ... tasks) { return new Series (this.executor_, tasks); }

  /// @}

  /// @{ Collections

  public <T> ForEach <T> forEach (Task <T> task)
  {
    return new ForEach<T> (this.executor_, task);
  }

  /// @}
}
