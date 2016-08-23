package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class QueueTest
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
  public void testPushSingleConcurrency () throws Exception
  {
    this.runPushTest (1);

    for (int i = 0; i < this.size_; ++ i)
      Assert.assertEquals ((Integer)(i + 1), this.nums_.get (i));
  }

  @Test
  public void testPushMultiConcurrency () throws Exception
  {
    this.runPushTest (3);
  }

  private void runPushTest (int concurrency) throws Exception
  {
    final Queue queue = new Queue (Executors.newCachedThreadPool (), concurrency);
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
        int index = (Integer)result;
        nums_.add (index);
      }
    };

    queue.setOnDrainListener (new Queue.OnDrainListener ()
    {
      @Override
      public void onDrain (Queue queue)
      {
        synchronized (QueueTest.this)
        {
          isCalled_ = true;
          QueueTest.this.notify ();
        }
      }
    });

    for (int i = 1; i <= this.size_; ++ i)
      queue.push (new CounterTask (i), callback);

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
      callback.done (this.index_);
    }
  }
}
