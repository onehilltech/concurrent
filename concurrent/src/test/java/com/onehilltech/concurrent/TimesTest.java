package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class TimesTest
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
    final Times times = new Times (
        Executors.newCachedThreadPool (),
        new Task () {
          int count_ = 0;

          @Override
          public void run (Object index, CompletionCallback callback)
          {
            Assert.assertEquals (count_ ++, index);
            callback.done ("DONE");
          }
        });

    final int loops = 4;

    synchronized (times)
    {
      Future future = times.execute (loops, new CompletionCallback ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertTrue ((result instanceof ArrayList));

          ArrayList <Object> a = (ArrayList<Object>)result;

          Assert.assertEquals (loops, a.size ());

          for (Object obj : a)
            Assert.assertEquals (obj, "DONE");

          callbackCalled_ = true;

          synchronized (times)
          {
            times.notify ();
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
        times.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteFail () throws Exception
  {
    final Times times = new Times (
        Executors.newCachedThreadPool (),
        new Task () {
          @Override
          public void run (Object unused, CompletionCallback callback)
          {
            callback.fail (new Exception ("IDK"));
          }
        });

    synchronized (times)
    {
      Future future = times.execute (7, new CompletionCallback ()
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

          synchronized (times)
          {
            times.notify ();
          }
        }

        @Override
        public void onCancel ()
        {
          Assert.fail ();
        }
      });

      if (!future.isDone ())
        times.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }

  @Test
  public void testExecuteCancel () throws Exception
  {
    final Times times = new Times (
        Executors.newCachedThreadPool (),
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

    synchronized (times)
    {
      Future future = times.execute (7, new CompletionCallback ()
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

          synchronized (times)
          {
            times.notify ();
          }
        }
      });

      future.cancel ();
      times.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
