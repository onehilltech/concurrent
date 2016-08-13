package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class RetryTest
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
    final Retry retry = new Retry (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);
            callback.onComplete ("DONE");
          }
        });

    final int retries = 4;

    synchronized (retry)
    {
      Future future = retry.execute (retries, new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertEquals ("DONE", result);
          callbackCalled_ = true;

          synchronized (retry)
          {
            retry.notify ();
          }
        }

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
      });

      if (!future.isDone ())
        retry.wait (5000);

      Assert.assertTrue (this.callbackCalled_);
      Assert.assertTrue (future.isDone ());
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final Retry retry = new Retry (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            callback.onFail (new Exception ("IDK"));
          }
        });

    synchronized (retry)
    {
      Future future = retry.execute (7, new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.fail ();
        }

        @Override
        public void onFail (Throwable e)
        {
          Assert.assertEquals (e.getMessage (), "IDK");
          callbackCalled_ = true;

          synchronized (retry)
          {
            retry.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        retry.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final Retry retry = new Retry (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            try
            {
              Thread.sleep (1000);
              callback.onComplete (null);
            }
            catch (InterruptedException e)
            {
              throw new RuntimeException (e);
            }
          }
        });

    synchronized (retry)
    {
      Future future = retry.execute (7, new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.fail ();
        }

        @Override
        public void onFail (Throwable e)
        {
          Assert.fail ();
        }

        @Override
        public void onCancel ()
        {
          callbackCalled_ = true;

          synchronized (retry)
          {
            retry.notify ();
          }
        }
      });

      future.cancel ();
      retry.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
