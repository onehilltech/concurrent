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

  public Waterfall waterfall (Waterfall.Task ... tasks)
  {
    return new Waterfall (this.executor_, tasks);
  }
}
