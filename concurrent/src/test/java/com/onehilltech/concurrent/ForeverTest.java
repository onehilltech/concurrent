package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class ForeverTest
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
    final Forever forever = new Forever (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);
            callback.done ("DONE");
          }
        });

    synchronized (forever)
    {
      Future future = forever.execute (new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          callbackCalled_ = true;

          synchronized (forever)
          {
            forever.notify ();
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
        forever.wait (1000);

      Assert.assertFalse (this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final Forever forever = new Forever (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            callback.fail (new Exception ("IDK"));
          }
        });

    synchronized (forever)
    {
      Future future = forever.execute (new CompletionCallback ()
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

          synchronized (forever)
          {
            forever.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        forever.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final Forever forever = new Forever (
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

    synchronized (forever)
    {
      Future future = forever.execute (new CompletionCallback ()
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

          synchronized (forever)
          {
            forever.notify ();
          }
        }
      });

      future.cancel ();
      forever.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
