package com.onehilltech.concurrent;

public abstract class Task <T>
{
  private String name_;

  /**
   * Create an unnamed task.
   */
  public Task ()
  {

  }

  /**
   * Create a new task.
   *
   * @param name
   */
  public Task (String name)
  {
    this.name_ = name;
  }

  public String getName ()
  {
    return this.name_;
  }

  /**
   * Run the task on the item.
   *
   * @param item
   */
  public abstract void run (T item, CompletionCallback callback);
}
