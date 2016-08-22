concurrent
==========

[![](https://jitpack.io/v/onehilltech/concurrent.svg)](https://jitpack.io/#onehilltech/concurrent)
[![Build Status](https://travis-ci.org/onehilltech/concurrent.svg?branch=master)](https://travis-ci.org/onehilltech/concurrent)

Utility library of concurrent execution strategies for JVM and Android

* Inspired by [async](http://caolan.github.io/async) for JavaScript and Node.js.
* Execute collection iterators and control flow strategies concurrently in the background.
* Callbacks are notified when strategies are complete, cancelled, or error out.
* Android extensions execute callbacks on the UI thread

## Installation

### Gradle

```
buildscript {
  repositories {
    maven { url "https://jitpack.io" }
  }
}

dependencies {
  # Select the dependency based on your project. You must not specify both modules
  # in your project.
  
  # for JVM projects
  compile com.github.onehilltech.concurrent:concurrent-core:x.y.z
  
  # for Android projects
  compile com.github.onehilltech.concurrent:concurrent-android:x.y.z
}
```

## Quick Start

Each strategy is implemented atop an `Executor` object. There are two ways to 
execute a concurrent strategy. The first method is to use the default
executor:

```java
Concurrent.getDefault ().series (
  new Task ("task-1") {
    @Override
    public void run (Object unused, CompletionCallback callback) {
      callback.done ("1");
    }
  },
  new Task ("task-2") {
    @Override
    public void run (Object unused, CompletionCallback callback) {
      callback.done ("2");
    }  
  }).execute (new CompleteCallack <Map <String, Object>> () {
    @Override
    public void onComplete (Map <String, Object> result) {
      // result.get ("task-1") equals "1"
      // result.get ("task-2") equals "2"
    }

    @Override
    public void onFail (Throwable e)
    {
      // one of the tasks failed
    }

    @Override
    public void onCancel ()
    {
      // the series of tasks where cancelled
    }  
  });
```

The second method is to create the concurrent strategy on an existing 
`Executor` object:

```java
Series series = new Series (executor,
  new Task ("task-1") {
    @Override
    public void run (Object unused, CompletionCallback callback) {
      callback.done ("1");
    }
  },
  new Task ("task-2") {
    @Override
    public void run (Object unused, CompletionCallback callback) {
      callback.done ("2");
    }  
  }).execute (new CompleteCallack <Map <String, Object>> () {
    @Override
    public void onComplete (Map <String, Object> result) {
      // result.get ("task-1") equals "1"
      // result.get ("task-2") equals "2"
    }

    @Override
    public void onFail (Throwable e)
    {
      // one of the tasks failed
    }

    @Override
    public void onCancel ()
    {
      // the series of tasks where cancelled
    }  
  });
```

### Chaining Concurrent Strategies

Concurrent strategies can be chained together. This is because the `CompletionCallback`
parameter in the `Task.run` method can be passed as the `CompletionCallback`
parameter to the `Task.execute` method.

Here is an example of downloading a result, and then iterating over the
values in the result:

```java
Concurrent.getDefault ().waterfall (
  new Task ("download-users") {
    @Override
    public void run (Object lastResult, CompletionCallback callback) {
      List <User> users;
      
      // GET /users
      
      callback.done (users);
    }
  },
  new Task ("save-users") {
    @Override
    public void run (Object lastResult, CompletionCallback callback) {
      List <User> users = (List <User>)lastResult;
      
      Concurrent.getDefault ().forEach (
        new Task <User> () {
          @Override
          public void run (User user, CompletionCallack callback) {
            // save the user
            callback.done ();
          }
        }).execute (users, callback);
    }  
  }).execute (new CompleteCallack <Map <String, Object>> () {
    @Override
    public void onComplete (Object lastResult) { 
      // the tasks are complete
    }

    @Override
    public void onFail (Throwable e)
    {
      // one of the tasks failed
    }

    @Override
    public void onCancel ()
    {
      // the series of tasks where cancelled
    }  
  });
```

## Next Steps

For more details on the strategies supports in 
[Concurrent](https://github.com/onehilltech/concurrent), please refer to the
documentation of [async](http://caolan.github.io/async). Each strategy in 
[Concurrent](https://github.com/onehilltech/concurrent) mirrors the behavior
of its corresponding strategy in [async](http://caolan.github.io/async).

Happy Coding!
