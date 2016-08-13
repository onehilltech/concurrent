package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

public class ConcurrentTest
{
  private final Task singleTask_ = new Task ()
  {
    @Override
    public void run (Object item, CompletionCallback callback)
    {

    }
  };

  private final Conditional conditional_ = new Conditional ()
  {
    @Override
    public boolean evaluate ()
    {
      return false;
    }
  };

  @Test
  public void testConstructor ()
  {
    Assert.assertEquals (ThreadPoolExecutor.class, Concurrent.getInstance ().getExecutor ().getClass ());
  }

  @Test
  public void testFactoryMethods ()
  {
    // Control Flow
    Assert.assertEquals (DoUntil.class, Concurrent.getInstance ().doUntil (this.conditional_, this.singleTask_).getClass ());
    Assert.assertEquals (Forever.class, Concurrent.getInstance ().forever (this.singleTask_).getClass ());
    Assert.assertEquals (Parallel.class, Concurrent.getInstance ().parallel (this.singleTask_).getClass ());
    Assert.assertEquals (Race.class, Concurrent.getInstance ().race (this.singleTask_).getClass ());
    Assert.assertEquals (Retry.class, Concurrent.getInstance ().retry (this.singleTask_).getClass ());
    Assert.assertEquals (Series.class, Concurrent.getInstance ().series (this.singleTask_).getClass ());
    Assert.assertEquals (Times.class, Concurrent.getInstance ().times (this.singleTask_).getClass ());
    Assert.assertEquals (Until.class, Concurrent.getInstance ().until (this.conditional_, this.singleTask_).getClass ());
    Assert.assertEquals (Waterfall.class, Concurrent.getInstance ().waterfall (this.singleTask_).getClass ());
    Assert.assertEquals (While.class, Concurrent.getInstance ().whilst (this.conditional_, this.singleTask_).getClass ());
    
    // Collections
    Assert.assertEquals (ForEach.class, Concurrent.getInstance ().forEach (this.singleTask_).getClass ());
  }
}
