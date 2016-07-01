package com.onehilltech.concurrent;

public interface Future
{
  boolean isDone ();
  void cancel ();
}
