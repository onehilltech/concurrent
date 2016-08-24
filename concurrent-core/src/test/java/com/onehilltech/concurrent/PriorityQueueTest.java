package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class PriorityQueueTest
{
  private boolean isCalled_;
  private final ArrayList <Integer> nums_ = new ArrayList<> ();
  private int size_;

  @Before
  public void setup ()
  {
    this.size_ = 10;
    this.isCalled_ = false;
  }

  @Test
  public void testAscendingPriority () throws Exception
  {
    this.runPushTest (1, PriorityQueue.ASCENDING_PRIORITY);

    for (int i = 0; i < this.size_; ++ i)
    {
      int actual = this.nums_.get (i);
      Assert.assertEquals (i, actual);
    }
  }

  @Test
  public void testDescendingPriority () throws Exception
  {
    this.runPushTest (1, PriorityQueue.DESCENDING_PRIORITY);

    for (int i = 0; i < this.size_; ++ i)
    {
      int actual = this.nums_.get (i);
      Assert.assertEquals ((this.nums_.size () - i - 1), actual);
    }
  }

  private void runPushTest (int concurrency, PriorityQueue.PriorityComparator comparator)
      throws Exception
  {
    final PriorityQueue queue =
        new PriorityQueue (
            Executors.newCachedThreadPool (),
            concurrency,
            comparator);

    final CompletionCallback callback = new CompletionCallback ()
    {
      @Override
      protected void onFail (Throwable reason)
      {

      }

      @Override
      protected void onCancel ()
      {

      }

      @Override
      protected void onComplete (Object result)
      {

      }
    };

    queue.setOnDrainListener (new Queue.OnDrainListener ()
    {
      @Override
      public void onDrain (Queue queue)
      {
        synchronized (PriorityQueueTest.this)
        {
          isCalled_ = true;
          PriorityQueueTest.this.notify ();
        }
      }
    });

    for (int i = 0; i < this.size_; ++ i)
      queue.push (i, new CounterTask (i), callback);

    synchronized (this)
    {
      this.wait (5000);
    }

    Assert.assertTrue (this.isCalled_);
    Assert.assertEquals (this.size_, this.nums_.size ());
  }

  class CounterTask extends Task
  {
    private int index_;

    public CounterTask (int i)
    {
      this.index_ = i;
    }

    @Override
    public void run (Object item, CompletionCallback callback)
    {
      nums_.add (this.index_);

      callback.done (this.index_);
    }
  }
}
