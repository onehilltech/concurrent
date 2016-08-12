package com.onehilltech.concurrent;

/**
 * Single unit of work used by the different concurrent strategies.
 *
 * @param <T>
 */
public abstract class Task <T>
{
  /// Name of the task.
  private String name_;

  /**
   * Default constructor.
   *
   * The constructed task is an unnamed task.
   */
  public Task ()
  {

  }

  /**
   * Initializing constructor.
   *
   * The constructed task is a named task.
   *
   * @param name
   */
  public Task (String name)
  {
    this.name_ = name;
  }

  /**
   * Get the name of the task.
   *
   * @return
   */
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
