package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class SomeTest
{
  private boolean callbackCalled_;

  @Before
  public void setup ()
  {
    this.callbackCalled_ = false;
  }

  @Test
  public void testExecuteAndFound () throws Exception
  {
    Integer[] nums = {1, 2, 3, 4, 5};

    Future future =
        new Some<> (Executors.newCachedThreadPool (),
                       new Task<Integer> ()
                       {
                         @Override
                         public void run (Integer item, CompletionCallback callback)
                         {
                           callback.done (item == 5);
                         }
                       }).execute (Arrays.asList (nums), new CompletionCallback ()
        {
          @Override
          public void onFail (Throwable e)
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
            Assert.assertEquals (true, result);
            callbackCalled_ = true;

            synchronized (SomeTest.this)
            {
              SomeTest.this.notify ();
            }
          }
        });

    synchronized (this)
    {
      this.wait ();

      Assert.assertTrue (future.isDone ());
      Assert.assertTrue (this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteAndNotFound () throws Exception
  {
    Integer[] nums = {1, 2, 3, 4, 5};

    Future future =
        new Some<> (Executors.newCachedThreadPool (),
                    new Task<Integer> ()
                    {
                      @Override
                      public void run (Integer item, CompletionCallback callback)
                      {
                        callback.done (item == 6);
                      }
                    }).execute (Arrays.asList (nums), new CompletionCallback ()
        {
          @Override
          public void onFail (Throwable e)
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
            Assert.assertEquals (false, result);
            callbackCalled_ = true;

            synchronized (SomeTest.this)
            {
              SomeTest.this.notify ();
            }
          }
        });

    synchronized (this)
    {
      this.wait ();

      Assert.assertTrue (future.isDone ());
      Assert.assertTrue (this.callbackCalled_);
    }
  }
}
