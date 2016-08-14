package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class DoWhileTest
{
  private boolean callbackCalled_;
  private int remaining_;

  @Before
  public void setup ()
  {
    this.callbackCalled_ = false;
    this.remaining_ = 0;
  }

  @Test
  public void testExecute () throws Exception
  {
    final DoWhile doWhile = new DoWhile (
        Executors.newCachedThreadPool (),
        new Conditional ()
        {
          @Override
          public boolean evaluate ()
          {
            return remaining_ > 0;
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);

            // Decrement the remaining.
            -- remaining_;

            callback.done ("DONE");
          }
        });

    synchronized (doWhile)
    {
      Future future = doWhile.execute (new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertEquals ("DONE", result);
          callbackCalled_ = true;

          synchronized (doWhile)
          {
            doWhile.notify ();
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
        doWhile.wait (5000);

      Assert.assertTrue (this.callbackCalled_);
      Assert.assertEquals (-1, this.remaining_);
      Assert.assertTrue (future.isDone ());
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final DoWhile doWhile = new DoWhile (
        Executors.newCachedThreadPool (),
        new Conditional ()
        {
          @Override
          public boolean evaluate ()
          {
            return true;
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            callback.fail (new Exception ("IDK"));
          }
        });

    synchronized (doWhile)
    {
      Future future = doWhile.execute (new CompletionCallback ()
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

          synchronized (doWhile)
          {
            doWhile.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        doWhile.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final DoWhile doWhile = new DoWhile (
        Executors.newCachedThreadPool (),
        new Conditional ()
        {
          @Override
          public boolean evaluate ()
          {
            return true;
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

    synchronized (doWhile)
    {
      Future future = doWhile.execute (new CompletionCallback ()
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

          synchronized (doWhile)
          {
            doWhile.notify ();
          }
        }
      });

      future.cancel ();
      doWhile.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
