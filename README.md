concurrent
==========

[![Build Status](https://travis-ci.org/onehilltech/concurrent.svg?branch=master)](https://travis-ci.org/onehilltech/concurrent.svg?branch=master)

Utility library of concurrent execution strategies for JVM and Android

* Inspired by [async](http://caolan.github.io/async) for JavaScript and Node.js.
* Execute collection iterators and control flow strategies concurrently in the background.
* Callbacks are notified when strategies are complete, cancelled, or error out.
* Android extensions execute callbacks on the UI thread

## Installation

### Gradle

```groovy
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
