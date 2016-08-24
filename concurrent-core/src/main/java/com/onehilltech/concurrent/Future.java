package com.onehilltech.concurrent;

public class Future
{
  /// Task manager that created the future.
  private TaskManager taskManager_;

  /***
   * Initializing constructor.
   *
   * @param taskManager
   */
  Future (TaskManager taskManager)
  {
    this.taskManager_ = taskManager;
  }

  /**
   * Test if the task is done.
   *
   * @return      True if done; otherwise false
   */
  public boolean isDone ()
  {
    return this.taskManager_.isDone ();
  }

  /**
   * Cancel the task.
   */
  public void cancel ()
  {
    this.taskManager_.cancel ();
  }
}
