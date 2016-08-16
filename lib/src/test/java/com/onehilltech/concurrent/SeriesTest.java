package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class SeriesTest
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
    final Series series = new Series (
        Executors.newCachedThreadPool (),
        new Task ("task-0") {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);
            System.err.println ("Running task one...");

            callback.done ("0");
          }
        },
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            Assert.assertNull (unused);
            System.err.println ("Running task two...");

            callback.done ("1");
          }
        });

    synchronized (series)
    {
      Future future = series.execute (new CompletionCallback <Map<String, Object>>()
      {
        @Override
        public void onComplete (Map <String, Object> result)
        {
          Assert.assertEquals (2, result.size ());
          Assert.assertEquals (result.get ("task-0"), "0");
          Assert.assertEquals (result.get ("1"), "1");

          callbackCalled_ = true;

          synchronized (series)
          {
            series.notify ();
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
        series.wait (10000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final Series series = new Series (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object lastResult, CompletionCallback callback)
          {
            callback.onComplete (1);
          }
        },
        new Task () {
          @Override
          public void run (Object lastResult, CompletionCallback callback)
          {
            callback.fail (new Exception ("IDK"));
          }
        });

    synchronized (series)
    {
      Future future = series.execute (new CompletionCallback ()
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

          synchronized (series)
          {
            series.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        series.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final Series series = new Series (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object lastResult, CompletionCallback callback)
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
        },
        new Task () {
          @Override
          public void run (Object lastResult, CompletionCallback callback)
          {
            callback.onComplete (lastResult);
          }
        });

    synchronized (series)
    {
      Future future = series.execute (new CompletionCallback ()
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

          synchronized (series)
          {
            series.notify ();
          }
        }
      });

      synchronized (series)
      {
        // Cancel the waterfall, and wait until notification.
        future.cancel ();
        series.wait (5000);
      }

      // Make sure the cancel callback is called.
      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
