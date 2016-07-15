package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class WaterfallTest
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
    final Waterfall waterfall = new Waterfall (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object lastResult)
          {
            Assert.assertNull (lastResult);
            System.err.println ("Running task one...");

            this.setResult (1);
          }
        },
        new Task () {
          @Override
          public void run (Object lastResult)
          {
            Assert.assertEquals (1, lastResult);
            System.err.println ("Running task one...");

            this.setResult (2);
          }
        });

    synchronized (waterfall)
    {
      Future future = waterfall.execute (new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertEquals (2, result);
          callbackCalled_ = true;

          synchronized (waterfall)
          {
            waterfall.notify ();
          }
        }

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
      });

      if (!future.isDone ())
        waterfall.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final Waterfall waterfall = new Waterfall (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object lastResult)
          {
            this.setResult (1);
          }
        },
        new Task () {
          @Override
          public void run (Object lastResult)
          {
            throw new RuntimeException ("IDK");
          }
        });

    synchronized (waterfall)
    {
      Future future = waterfall.execute (new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.fail ();
        }

        @Override
        public void onFail (Exception e)
        {
          Assert.assertEquals (e.getMessage (), "IDK");
          callbackCalled_ = true;

          synchronized (waterfall)
          {
            waterfall.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        waterfall.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final Waterfall waterfall = new Waterfall (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object lastResult)
          {
            try
            {
              Thread.sleep (1000);
            }
            catch (InterruptedException e)
            {
              throw new RuntimeException (e);
            }
          }
        },
        new Task () {
          @Override
          public void run (Object lastResult)
          {
            this.setResult (lastResult);
          }
        });

    synchronized (waterfall)
    {
      Future future = waterfall.execute (new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.fail ();
        }

        @Override
        public void onFail (Exception e)
        {
          Assert.fail ();
        }

        @Override
        public void onCancel ()
        {
          callbackCalled_ = true;

          synchronized (waterfall)
          {
            waterfall.notify ();
          }
        }
      });

      synchronized (waterfall)
      {
        // Cancel the waterfall, and wait until notification.
        future.cancel ();
        waterfall.wait (5000);
      }

      // Make sure the cancel callback is called.
      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
