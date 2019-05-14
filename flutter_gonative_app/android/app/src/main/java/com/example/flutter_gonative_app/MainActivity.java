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
