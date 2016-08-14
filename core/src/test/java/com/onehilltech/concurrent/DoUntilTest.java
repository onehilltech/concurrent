package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class DoUntilTest
{
  private boolean callbackCalled_;
  private int current_;

  @Before
  public void setup ()
  {
    this.callbackCalled_ = false;
    this.current_ = 0;
  }

  @Test
  public void testExecute () throws Exception
  {
    final DoUntil doUntil = new DoUntil (
        Executors.newCachedThreadPool (),
        new Conditional ()
        {
          @Override
          public boolean evaluate ()
          {
            return current_ > 0;
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);

            // Decrement the remaining.
            current_ ++;

            callback.done ("DONE");
          }
        });

    synchronized (doUntil)
    {
      Future future = doUntil.execute (new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertEquals ("DONE", result);
          callbackCalled_ = true;

          synchronized (doUntil)
          {
            doUntil.notify ();
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
        doUntil.wait ();

      Assert.assertTrue (this.callbackCalled_);
      Assert.assertEquals (1, this.current_);
      Assert.assertTrue (future.isDone ());
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final DoUntil doUntil = new DoUntil (
        Executors.newCachedThreadPool (),
        new Conditional ()
        {
          @Override
          public boolean evaluate ()
          {
            // We want to keep looping forever...
            return false;
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);
            callback.fail (new Exception ("IDK"));
          }
        });

    synchronized (doUntil)
    {
      Future future = doUntil.execute (new CompletionCallback ()
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

          synchronized (doUntil)
          {
            doUntil.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        doUntil.wait ();

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final DoUntil doUntil = new DoUntil (
        Executors.newCachedThreadPool (),
        new Conditional ()
        {
          @Override
          public boolean evaluate ()
          {
            return false;
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            try
            {
              Thread.sleep (1000);
              callback.done (null);
            }
            catch (InterruptedException e)
            {
              throw new RuntimeException (e);
            }
          }
        });

    synchronized (doUntil)
    {
      Future future = doUntil.execute (new CompletionCallback ()
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

          synchronized (doUntil)
          {
            doUntil.notify ();
          }
        }
      });

      future.cancel ();
      doUntil.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
