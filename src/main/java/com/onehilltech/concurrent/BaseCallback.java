package com.onehilltech.concurrent;

public interface BaseCallback
{
  void onFail (Exception e);
  void onCancel ();
}
