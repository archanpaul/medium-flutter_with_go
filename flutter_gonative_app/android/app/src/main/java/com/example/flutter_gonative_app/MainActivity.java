package com.example.flutter_gonative_app;

import android.os.Bundle;
import android.util.Log;

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
