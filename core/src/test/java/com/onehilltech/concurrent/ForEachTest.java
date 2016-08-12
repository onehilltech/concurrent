package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ForEachTest
{
  private boolean callbackCalled_;

  @Before
  public void setup ()
  {
    this.callbackCalled_ = false;
  }

  @Test
  public void testExecute () throws Exception
  {
    Integer[] nums = {1, 2, 3, 4, 5};
    final AtomicInteger sum = new AtomicInteger (0);

    Future future =
        new ForEach<Integer> (Executors.newCachedThreadPool (),
                       new Task<Integer> ()
                       {
                         @Override
                         public void run (Integer item, CompletionCallback callback)
                         {
                           sum.addAndGet (item);

                           callback.onComplete (null);
                         }
                       }).execute (Arrays.asList (nums), new CompletionCallback ()
        {
          @Override
          public void onFail (Exception e)
          {
            Assert.fail ();
          }

          @Override
          public void onCancel ()
          {
            Assert.fail ();
          }

          @Override
          public void onComplete (Object result)
          {
            synchronized (ForEachTest.this)
            {
              callbackCalled_ = true;
              ForEachTest.this.notify ();
            }
          }
        });

    synchronized (this)
    {
      this.wait ();

      Assert.assertEquals (15, sum.get ());
      Assert.assertTrue (future.isDone ());
      Assert.assertTrue (this.callbackCalled_);
    }
  }
}
