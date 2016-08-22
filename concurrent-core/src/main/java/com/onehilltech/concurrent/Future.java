package com.onehilltech.concurrent;

public class Future
{
  private TaskManager taskManager_;

  Future (TaskManager taskManager)
  {
    this.taskManager_ = taskManager;
  }

  public boolean isDone ()
  {
    return this.taskManager_.isDone ();
  }

  public void cancel ()
  {
    this.taskManager_.cancel ();
  }
}
