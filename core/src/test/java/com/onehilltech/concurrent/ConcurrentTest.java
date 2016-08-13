package com.onehilltech.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

public class ConcurrentTest
{
  @Test
  public void testConstructor ()
  {
    Assert.assertEquals (ThreadPoolExecutor.class,
                         Concurrent.getInstance ().getExecutor ().getClass ());
  }
}
