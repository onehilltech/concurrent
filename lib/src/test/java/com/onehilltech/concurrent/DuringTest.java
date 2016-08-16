package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class DuringTest
{
  private boolean callbackCalled_;
  private int remaining_;

  @Before
  public void setup ()
  {
    this.callbackCalled_ = false;
    this.remaining_ = 3;
  }

  @Test
  public void testExecute () throws Exception
  {
    final During during = new During (
        Executors.newCachedThreadPool (),
        new ConditionalTask ()
        {
          @Override
          public void evaluate (ConditionalCallback callback)
          {
            callback.done (remaining_ > 0);
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

    synchronized (during)
    {
      Future future = during.execute (new CompletionCallback <Object> ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertEquals ("DONE", result);
          callbackCalled_ = true;

          synchronized (during)
          {
            during.notify ();
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
        during.wait (5000);

      Assert.assertTrue (this.callbackCalled_);
      Assert.assertEquals (0, this.remaining_);
      Assert.assertTrue (future.isDone ());
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final During during = new During (
        Executors.newCachedThreadPool (),
        new ConditionalTask ()
        {
          @Override
          public void evaluate (ConditionalCallback callback)
          {
            callback.done (true);
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            callback.fail (new Exception ("IDK"));
          }
        });

    synchronized (during)
    {
      Future future = during.execute (new CompletionCallback <Object> ()
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

          synchronized (during)
          {
            during.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        during.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final During during = new During (
        Executors.newCachedThreadPool (),
        new ConditionalTask ()
        {
          @Override
          public void evaluate (ConditionalCallback callback)
          {
            callback.done (true);
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

    synchronized (during)
    {
      Future future = during.execute (new CompletionCallback <Object> ()
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

          synchronized (during)
          {
            during.notify ();
          }
        }
      });

      future.cancel ();
      during.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
