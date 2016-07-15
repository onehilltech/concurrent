package com.onehilltech.concurrent;

public abstract class Task <T>
{
  private String name_;

  private Object result_;

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

  protected void setResult (Object result)
  {
   this.result_ = result;
  }

  public Object getResult ()
  {
    return this.result_;
  }

  public boolean hasResult ()
  {
    return this.result_ != null;
  }

  /**
   * Run the task on the item.
   *
   * @param item
   */
  public abstract void run (T item);
}
