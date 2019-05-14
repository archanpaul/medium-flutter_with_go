# Using Go code in Flutter app

[Flutter](https://flutter.dev/) has made it possible to develope cross platform mobile app with native performance. Till we had [Flutter](https://flutter.dev/) we used to write the platform specific UI code of mobile app using Java/Kotlin for [Android](https://www.android.com/intl/en_in/) and Objective-C/Swift for [iOS](https://www.apple.com/ios).

While working on various full-stack mobile-app development projects at [Arputer Technologies](https://www.arputer.com/) we wrote platform independent business-logic or algorithm code in [Go](https://golang.org/) and used Google's [Gomobile](https://godoc.org/golang.org/x/mobile/cmd/gomobile) tool to create Android/iOS natively compiled libraries which are called from Java/Kotlin or Objective-C/Swift UI depending on the platform. This approach helped us to write (and test) core business logic code once and use the same in multiple platforms (Android, iOS and .. Linux, Mac). While working with Flutter we wanted to reuse the Go codebase/API and we found it easy to use the [Gomobile](https://godoc.org/golang.org/x/mobile/cmd/gomobile) generated Android/iOS libraries in Flutter using [Flutter platform-channel](https://flutter.dev/docs/development/platform-integration/platform-channels).

In this article I will give an example of trivial use-case :

* A simple Go app which receives an integer variable from Flutter through platform-channel and returns an incremented value of it.
* A simple flutter app which sends integer to native Go API and update it's statefull 

In this article we will assume that **the development workstation is [Linux](https://www.linuxfoundation.org/)** and running any of the latest Linux distributions and **Android** is used for testing the mobile app developed.

## Development workstation setup

* Ensure that Go compiler is installed.
    * `go 1.12.2 linux/amd64`
* Ensure that Android SDK is installed.
    * `Android 9.0 (Pie) - Android SDK Platform 28.`
* Ensure that Android NDK is installed.
    * `NDK 19.2.x`
* Android Emulator is installed for testing the app.
    * `Google Play Intel x86 Atom System Image.`

### Environment setup for Go

First we need to prepare the environment for Gomobile development. 
Latest version of Go is recommended to be installed 

```bash
~$ go version
go version go1.12.2 linux/amd64
```

Update environment variables (your path might be different, hence update accordingly).

```bash
~$ export GOPATH=$HOME/workspace/go-packages
~$ export ANDROID_HOME=$HOME/workspace/android-sdk/
~$ export ANDROID_SDK_ROOT=$ANDROID_HOME
~$ export ANDROID_NDK_ROOT=$ANDROID_HOME/ndk-bundle
~$ export PATH=$PATH:$GOPATH/bin:$ANDROID_HOME/platform-tools/
```

*Please note, for gomobile work correctly you need have above environment variables set and exported.*

Install [gomobile](https://godoc.org/golang.org/x/mobile/cmd/gomobile).

```bash
~$ go get golang.org/x/mobile/cmd/gomobile
~$ gomobile version
gomobile version +3e0bab5 ...
~$ gomobile init
```

### Environment setup for flutter

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
