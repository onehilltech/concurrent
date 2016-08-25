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

  private final ConditionalTask conditionalTask_ = new ConditionalTask ()
  {
    @Override
    public void evaluate (ConditionalCallback callback)
    {

    }
  };

  @Test
  public void testConstructor ()
  {
    Assert.assertEquals (ThreadPoolExecutor.class, Concurrent.getDefault ().getExecutor ().getClass ());
  }

  @Test
  public void testFactoryMethods ()
  {
    Assert.assertEquals (Constant.class, Concurrent.getDefault ().constant ("max", 7).getClass ());
    Assert.assertEquals (Constant.class, Concurrent.getDefault ().constant (10).getClass ());

    // Control Flow
    Assert.assertEquals (DoUntil.class, Concurrent.getDefault ().doUntil (this.conditional_, this.singleTask_).getClass ());
    Assert.assertEquals (DoWhile.class, Concurrent.getDefault ().doWhile (this.conditional_, this.singleTask_).getClass ());
    Assert.assertEquals (During.class, Concurrent.getDefault ().during (this.conditionalTask_, this.singleTask_).getClass ());
    Assert.assertEquals (Forever.class, Concurrent.getDefault ().forever (this.singleTask_).getClass ());
    Assert.assertEquals (Parallel.class, Concurrent.getDefault ().parallel (this.singleTask_).getClass ());

    Assert.assertEquals (PriorityQueue.class, Concurrent.getDefault ().priorityQueue (1).getClass ());
    Assert.assertEquals (PriorityQueue.class, Concurrent.getDefault ().priorityQueue (1, PriorityQueue.DESCENDING_PRIORITY).getClass ());
    Assert.assertEquals (PriorityQueue.class, Concurrent.getDefault ().priorityQueue (1, PriorityQueue.DESCENDING_PRIORITY, 1).getClass ());

    Assert.assertEquals (Queue.class, Concurrent.getDefault ().queue (1).getClass ());
    Assert.assertEquals (Race.class, Concurrent.getDefault ().race (this.singleTask_).getClass ());
    Assert.assertEquals (Retry.class, Concurrent.getDefault ().retry (this.singleTask_).getClass ());
    Assert.assertEquals (Series.class, Concurrent.getDefault ().series (this.singleTask_).getClass ());
    Assert.assertEquals (Times.class, Concurrent.getDefault ().times (this.singleTask_).getClass ());
    Assert.assertEquals (Until.class, Concurrent.getDefault ().until (this.conditional_, this.singleTask_).getClass ());
    Assert.assertEquals (Waterfall.class, Concurrent.getDefault ().waterfall (this.singleTask_).getClass ());
    Assert.assertEquals (While.class, Concurrent.getDefault ().whilst (this.conditional_, this.singleTask_).getClass ());

    // Collections
    Assert.assertEquals (Every.class, Concurrent.getDefault ().every (this.singleTask_).getClass ());
    Assert.assertEquals (ForEach.class, Concurrent.getDefault ().forEach (this.singleTask_).getClass ());
    Assert.assertEquals (Some.class, Concurrent.getDefault ().some (this.singleTask_).getClass ());
  }
}
