# Using Go Library in Flutter

This article is originally published [here](https://www.arputer.com/using-go-library-in-flutter).

[Flutter](https://flutter.dev/) has made it possible to develope cross platform mobile app with native performance. Till we had [Flutter](https://flutter.dev/) we used to write the platform specific UI code of mobile app using Java/Kotlin for [Android](https://www.android.com/intl/en_in/) and Objective-C/Swift for [iOS](https://www.apple.com/ios). Now with Flutter we can use same codebase for App/UI for multiple platforms.

While working on various full-stack mobile-app development projects at [Arputer Technologies](https://www.arputer.com/) we wrote tons of platform independent business-logic or algorithm code in [Go](https://golang.org/) and used Google's [Gomobile](https://godoc.org/golang.org/x/mobile/cmd/gomobile) tool to create Android/iOS natively compiled libraries which are called from Java/Kotlin or Objective-C/Swift UI depending on the platform. This approach helped us to write (and test) core business logic code once and use the same in multiple platforms (Android, iOS and .. Linux, Mac). While working with Flutter we wanted to reuse the same Go codebase/API and we found it easy to use the [Gomobile](https://godoc.org/golang.org/x/mobile/cmd/gomobile) generated Android/iOS libraries in Flutter using [Flutter platform-channel](https://flutter.dev/docs/development/platform-integration/platform-channels).

In this article we will learn how to integrate Go library code into Flutter and use it in cross platform scenario. We will try to achieve following :

* A simple Go app which receives an integer variable from Flutter through platform-channel and returns an incremented value of it.
* A simple flutter app which sends integer to native Go API and update it's stateful widget. We will modify the wellknow *counter app* which is generated from template when you create Flutter app.

In this article we will assume that the development workstation is **[Linux](https://www.linuxfoundation.org/)** and running any of the latest Linux distributions and **Android** is used for testing the mobile app to be developed.

## Development workstation setup :

* Ensure that Go compiler is installed.
    * eg: `go 1.12.2 linux/amd64`
* Ensure that Android SDK is installed.
    * eg: `Android 9.0 (Pie) - Android SDK Platform 28.`
* Ensure that Android NDK is installed.
    * eg: `NDK 19.2.x`
* Android Emulator is installed for testing the app.
    * eg: `Google Play Intel x86 Atom System Image.`

### Environment setup for Go :

First we need to prepare the environment for Gomobile development. 
Latest version of Go is recommended to be installed 

```bash
~$ go version
go version go1.12.2 linux/amd64
```

### Environment variables :

Update environment variables (your path might be different, hence update accordingly).

```bash
~$ export GOPATH=$HOME/workspace/go-packages
~$ export ANDROID_HOME=$HOME/workspace/android-sdk/
~$ export ANDROID_SDK_ROOT=$ANDROID_HOME
~$ export ANDROID_NDK_ROOT=$ANDROID_HOME/ndk-bundle
~$ export PATH=$PATH:$GOPATH/bin:$ANDROID_HOME/platform-tools/
```

*Please note for gomobile to work correctly, you need have above environment variables set and exported.*

### [Gomobile](https://godoc.org/golang.org/x/mobile/cmd/gomobile) installation :

```bash
~$ go get golang.org/x/mobile/cmd/gomobile
~$ gomobile version
gomobile version +3e0bab5 ...
~$ gomobile init
```

### Environment setup for flutter :

```bash
~$ git clone -b master https://github.com/flutter/flutter.git ~/workspace/flutter-sdk
~$ mkdir $HOME/workspace/flutter-sdk/pub_cache
~$ export PATH=$PATH:$FLUTTER_ROOT/bin:$PUB_CACHE/bin
~$ flutter doctor
Doctor summary (to see all details, run flutter doctor -v):
[✓] Flutter (Channel stable, v1.5.4-hotfix.2, on Linux, locale en_IN.UTF-8)
 
[✓] Android toolchain - develop for Android devices (Android SDK version 28.0.3)
[✓] Android Studio (version 3.4)
[✓] VS Code (version 1.33.1)
[✓] Connected device (1 available)

• No issues found!
```
## Create our Go Library for Flutter

```bash
~$ mkdir -p $GOPATH/src/gonative-lib
```

Create `$GOPATH/src/gonative-lib/data-processor.go`

```go
package gonativelib

import (
    "errors"
)

type (
    // DataProcessor :
    DataProcessor struct {
    	// add fields here
    }
)

// Increment : Increment the int received as an argument.
func (p *DataProcessor) Increment(data int) (int, error) {
    if data < 0 {
	    // Return error if data is negative. This will
	    // result exception in Android side.
	    return data, errors.New("data can't be negative")
    }
    return (data + 1), nil
}
```

### Compile and create Android library :

```bash
~$ cd $GOPATH/src/gonative-lib
~$ gomobile bind --target android
```

Following files are generated by Gomobile command.

* `gonativelib.aar` : Android library to be used in Flutter app.
    * `Gonativelib` : class corresponding to the Go package.
    * `DataProcessor` : class corresponding to Go struct, which is to be used in app.
* `gonativelib-sources.jar` : Java sources for reference.

### Data types supported by Gomobile :

At present, a subset of Go types are supported by Gomobile. For more information refer to [gobind](https://godoc.org/golang.org/x/mobile/cmd/gobind).

- Signed integer and floating point types.

- String and boolean types.

- Byte slice types. Note that byte slices are passed by reference,
  and support mutation.

- Any function type all of whose parameters and results have
  supported types. Functions must return either no results,
  one result, or two results where the type of the second is
  the built-in 'error' type.

- Any interface type, all of whose exported methods have
  supported function types.

- Any struct type, all of whose exported methods have
  supported function types and all of whose exported fields
  have supported types.

## Create Flutter app

Create flutter app from from default template.

```bash
~$ cd ~/workspace/
~$ flutter create flutter_gonative_app
~$ cd ~/workspace/flutter_gonative_app
```

When you run this default app from your favorite IDE you should be able to see the familiar app with stateful widget containing the floating-button. On clicking the floating-button the counter value get updated and shown in UI.

In the default app, the counter is updated in _incrementCounter()

```dart
  void _incrementCounter() {
    setState(() {
      // This call to setState tells the Flutter framework that something has
      // changed in this State, which causes it to rerun the build method below
      // so that the display can reflect the updated values. If we changed
      // _counter without calling setState(), then the build method would not be
      // called again, and so nothing would appear to happen.
      _counter++;
    });
  }
```
We will updated this function to call Go Library API in following sections.

## Add Go library (Android) to Flutter app :

First, add Android `gonativelib` which we have created in the previous step, to the Flutter app.

To add the Android `gonativelib` which we have created in the previous step, to the Flutter app, create `libs` folder in Android code of Flutter app and copy  `gonativelib` Android `aar` file into this folder.

```bash
~$ mkdir ~/workspace/flutter_gonative_app/android/app/src/main/libs/
~$ cp $GOPATH/src/gonative-lib/gonativelib.aar ~/workspace/flutter_gonative_app/android/app/src/main/libs/
```

Now update `~/workspace/flutter_gonative_app/android/app/build.gradle` with following :

```java
repositories{
    flatDir{
         dirs 'src/main/libs'
    }
}

dependencies {
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    api(name:'gonativelib', ext:'aar')
}
```

Corresponding `git-diff`.

```diff
~$ git diff flutter_gonative_app/android/app/build.gradle
diff --git a/flutter_gonative_app/android/app/build.gradle b/flutter_gonative_app/android/app/build.gradle
index 51d0d1d..4174a0e 100644
--- a/flutter_gonative_app/android/app/build.gradle
+++ b/flutter_gonative_app/android/app/build.gradle
@@ -54,8 +54,15 @@ flutter {
     source '../..'
 }
 
+repositories{
+    flatDir{
+         dirs 'src/main/libs'
+    }
+}
+
 dependencies {
     testImplementation 'junit:junit:4.12'
     androidTestImplementation 'com.android.support.test:runner:1.0.2'
     androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
+    api(name:'gonativelib', ext:'aar')
 }

```

Now the classes provided by `gonativelib` should be accessible from Android.  
Update `~/workspace/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java` with following to access `gonativelib.DataProcessor`.

```java
package com.example.flutter_gonative_app;

import android.os.Bundle;

import gonativelib.DataProcessor;
import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;


public class MainActivity extends FlutterActivity {
  DataProcessor goNativeDataProcessor = new DataProcessor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
  }
}
```

Corresponding `git-diff`.

```diff
~$ git diff flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
diff --git a/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java b/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
index 2fb247b..cca3684 100644
--- a/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
+++ b/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
@@ -1,10 +1,15 @@
 package com.example.flutter_gonative_app;
 
 import android.os.Bundle;
+
+import gonativelib.DataProcessor;
 import io.flutter.app.FlutterActivity;
 import io.flutter.plugins.GeneratedPluginRegistrant;
 
+
 public class MainActivity extends FlutterActivity {
+  DataProcessor goNativeDataProcessor = new DataProcessor();
+
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);

```

Restart your flutter app to get it recompiled with the changes you have made.

## Add platform-channel to Flutter app

To call API of `gonativelib`, we need to use FLutter [Platform Channel](https://flutter.dev/docs/development/platform-integration/platform-channels). 

### Data types supported in platform-channel :

```
Dart	    Android            
===============================
null	    null               
bool	    java.lang.Boolean  
int         java.lang.Integer  
int         java.lang.Long     
double	    java.lang.Double   
String	    java.lang.String   
Uint8List   byte[]             
Int32List   int[]              
Int64List   long[]             
Float64List double[]           
List	    java.util.ArrayList
Map         java.util.HashMap  
```

### Add MethodChannel in Flutter/Dart :

Flutter app's `_MyHomePageState` class holds apps's current state. We need to create a MethodChannel though which we will be communicating with `gonalivelib`. The client and host sides of the Channel do the handshake through the channel name passed in the `MethodChannel constructor` of platform-channel. In our case, the channel name is `example.com/gonative`. This should be unique in the app.

In the Flutter side update `_incrementCounter()` function from `~/workspace/flutter_gonative_app/lib/main.dart`

```dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

...

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  static const platform = const MethodChannel('example.com/gonative');

  Future<void> _incrementCounter() async {
    int incrementedCounter;

    try {
      var arguments = Map();
      arguments["data"] = _counter;
      incrementedCounter =
          await platform.invokeMethod('dataProcessor_increment', arguments);
    } on PlatformException catch (e) {
      print("PlatformException: ${e.message}");
    }

    if (incrementedCounter != null) {
      setState(() {
        _counter = incrementedCounter;
      });
    }
  }

...
```

Corresponding `git-diff`.

```diff
~$ git diff flutter_gonative_app/lib/main.dart
diff --git a/flutter_gonative_app/lib/main.dart b/flutter_gonative_app/lib/main.dart
index f4ebf1d..c281921 100644
--- a/flutter_gonative_app/lib/main.dart
+++ b/flutter_gonative_app/lib/main.dart
@@ -1,4 +1,5 @@
 import 'package:flutter/material.dart';
+import 'package:flutter/services.dart';
 
 void main() => runApp(MyApp());
 
@@ -45,16 +46,25 @@ class MyHomePage extends StatefulWidget {
 
 class _MyHomePageState extends State<MyHomePage> {
   int _counter = 0;
+  static const platform = const MethodChannel('example.com/gonative');
 
-  void _incrementCounter() {
-    setState(() {
-      // This call to setState tells the Flutter framework that something has
-      // changed in this State, which causes it to rerun the build method below
-      // so that the display can reflect the updated values. If we changed
-      // _counter without calling setState(), then the build method would not be
-      // called again, and so nothing would appear to happen.
-      _counter++;
-    });
+  Future<void> _incrementCounter() async {
+    int incrementedCounter;
+
+    try {
+      var arguments = Map();
+      arguments["data"] = _counter;
+      incrementedCounter =
+          await platform.invokeMethod('dataProcessor_increment', arguments);
+    } on PlatformException catch (e) {
+      print("PlatformException: ${e.message}");
+    }
+
+    if (incrementedCounter != null) {
+      setState(() {
+        _counter = incrementedCounter;
+      });
+    }
   }
 
   @override
```
Let us now understand the magic happening in above code.

* Once the MethodChannel is established, Flutter can invoke a method (using *invokeMethod()*) by specifying a concrete method to call via String identifier (in our case *dataProcessor_increment*). The [invokeMethod](https://docs.flutter.io/flutter/services/MethodChannel/invokeMethod.html) returns a Future and may result an *exception* (hence wrapped in a try-catch block).

* Once the *Future* returns, `_counter` is updated in `setState()` call which in turn updates the Widget automatically.

* Please note, our `gonativelib` can return exception which is processed in Android/Java side and transparently forwarded to Flutter/Dart. The same exception can be addressed in `on PlatformException catch (e)`.

### Add MethodChannel in Android/Java :

Now in Android Java side update `MainActivity.onCreate` at `~/workspace/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java` with following :

```java
package com.example.flutter_gonative_app;

import android.os.Bundle;
import android.util.Log;

import gonativelib.DataProcessor;
import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  DataProcessor goNativeDataProcessor = new DataProcessor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);

    MethodChannel methodChannel = new MethodChannel(getFlutterView(), "example.com/gonative");
    methodChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
      @Override
      public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (methodCall.method.equals("dataProcessor_increment")) {
          if (!methodCall.hasArgument("data")) {
            result.error("dataProcessor_increment", "Send argument as Map<\"data\", int>", null);
            return;
          }
          try {
            Integer data = methodCall.argument("data");
            result.success(goNativeDataProcessor.increment(data.longValue()));
            return;
          } catch (Exception e) {
            result.error("dataProcessor_increment", e.getMessage(), null);
          }
        } else {
          result.notImplemented();
        }
      }
    });
  }
}
```

Corresponding `git-diff`

```diff
~$ git diff flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
diff --git a/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java b/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
index 204af8a..8893167 100644
--- a/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
+++ b/flutter_gonative_app/android/app/src/main/java/com/example/flutter_gonative_app/MainActivity.java
@@ -5,6 +5,8 @@ import android.util.Log;
 
 import gonativelib.DataProcessor;
 import io.flutter.app.FlutterActivity;
+import io.flutter.plugin.common.MethodCall;
+import io.flutter.plugin.common.MethodChannel;
 import io.flutter.plugins.GeneratedPluginRegistrant;
 
 public class MainActivity extends FlutterActivity {
@@ -14,5 +16,27 @@ public class MainActivity extends FlutterActivity {
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     GeneratedPluginRegistrant.registerWith(this);
+
+    MethodChannel methodChannel = new MethodChannel(getFlutterView(), "example.com/gonative");
+    methodChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
+      @Override
+      public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
+        if (methodCall.method.equals("dataProcessor_increment")) {
+          if (!methodCall.hasArgument("data")) {
+            result.error("dataProcessor_increment", "Send argument as Map<\"data\", int>", null);
+            return;
+          }
+          try {
+            Integer data = methodCall.argument("data");
+            result.success(goNativeDataProcessor.increment(data.longValue()));
+            return;
+          } catch (Exception e) {
+            result.error("dataProcessor_increment", e.getMessage(), null);
+          }
+        } else {
+          result.notImplemented();
+        }
+      }
+    });
   }
 }
```

* In the Android/Java side, we have added [MethodCallHandler](https://docs.flutter.io/javadoc/io/flutter/plugin/common/MethodChannel.MethodCallHandler.html) to the [methodChannel](https://docs.flutter.io/javadoc/io/flutter/plugin/common/MethodChannel.html) to handle method calls received from Flutter/Dart.

```java
methodChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        ...
    }
});
```
* [onMethodCall](https://docs.flutter.io/javadoc/io/flutter/plugin/common/MethodChannel.MethodCallHandler.html#onMethodCall-io.flutter.plugin.common.MethodCall-io.flutter.plugin.common.MethodChannel.Result-) handles specified method call received from Flutter along with arguments. The argument is recommended to be a Map in Flutter/Dart which can be decoded in Android/Java side as shown above. For more information about argument check [method-details documentation](https://docs.flutter.io/javadoc/io/flutter/plugin/common/MethodCall.html#argument-java.lang.String-).


Now, restart the app and when you click the floating-button, Flutter/Dart calls `MethodChannel` to `invokeMethod` with current state's `_counter`. On receiving the call in though predefined `MethodChannel`, Android/Java calls corresponding `GoNative` object to get the output and returns the same to Flutter.

## End note

In this article I have used a trivial example to demonstrate how to use Flutter's `platform-channel` to call GoNative api in the context of Android. Similar approach can be used in case of iOS.

What we learned in this article :

* Developing Go library API which can be source compiled for cross platforms.
* Integration of Go library into Flutter app (Android context).
* Use platform channel in Flutter app to call platform specific libraries (in our case Go native library).
* Send argument(s) to Go API from Flutter and return (synchronous) results back to Flutter for UI update.

The source code can be found from [Bitbucket/arputer](https://bitbucket.org/arputer/flutter_with_go/).

In the future article in this series I will try to demonstrate an example about, how to use [RxDart](https://github.com/ReactiveX/rxdart) and [EventChannel](https://docs.flutter.io/flutter/services/EventChannel-class.html) to achieve this. 

Stay tuned.