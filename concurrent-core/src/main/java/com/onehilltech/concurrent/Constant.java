package com.onehilltech.concurrent;

/**
 * @class Constant
 *
 * Task that completes with a constant value.
 */
public class Constant <T> extends Task
{
  private final T constant_;

  public Constant (T constant)
  {
    this.constant_ = constant;
  }

  public Constant (String name, T constant)
  {
    super (name);
    this.constant_ = constant;
  }

  @Override
  public void run (Object item, CompletionCallback callback)
  {
    callback.done (this.constant_);
  }
}
