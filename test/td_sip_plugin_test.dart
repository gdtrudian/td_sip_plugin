import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:td_sip_plugin/td_sip_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('td_sip_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

}
