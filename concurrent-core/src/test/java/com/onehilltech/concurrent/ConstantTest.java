package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

public class ConstantTest
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
        new Constant <> (10),
        new Task () {
          @Override
          public void run (Object lastResult, CompletionCallback callback)
          {
            Assert.assertEquals (10, lastResult);
            callback.done ();
          }
        });

    synchronized (this)
    {
      Future future = waterfall.execute (new CompletionCallback <Object> ()
      {
        @Override
        public void onComplete (Object result)
        {
          Assert.assertNull (result);
          callbackCalled_ = true;

          synchronized (ConstantTest.this)
          {
            ConstantTest.this.notify ();
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
        this.wait (5000);

      Assert.assertEquals (true, this.callbackCalled_);
    }
  }
}
