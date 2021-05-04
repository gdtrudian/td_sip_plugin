import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'td_sip_plugin.dart';

class TDDisplayView extends StatefulWidget {
  String placeholder;

  TDDisplayView({
    this.placeholder,
  });

  @override
  _TDDisplayViewState createState() => _TDDisplayViewState(this.placeholder);
}

class _TDDisplayViewState extends State<TDDisplayView> {
  String placeholder;

  _TDDisplayViewState(this.placeholder);

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    TdSipPlugin.addDisplayViewReceiver(onEvent: (event){
      if (event["eventName"] == "setPlaceholder") {
        if (placeholder != null) {
          _displayPlaceholder();
        }
      }
    });
  }

  void _displayPlaceholder() async {
    ByteData byteData = await DefaultAssetBundle.of(context).load(placeholder);
    List<int> bytes = byteData.buffer.asUint8List();
    String encodeString = base64Encode(bytes);
    String base64 = "data:image/png;base64," + encodeString;
    if (defaultTargetPlatform == TargetPlatform.iOS) {
      TdSipPlugin.setPlaceholder(base64);
    } else {
      TdSipPlugin.setPlaceholder(encodeString);
    }
  }

  @override
  Widget build(BuildContext context) {
    return defaultTargetPlatform == TargetPlatform.iOS
        ? UiKitView(
            viewType: "TDDisplayView",
            creationParamsCodec: StandardMessageCodec(),
          )
        : AndroidView(viewType: "TDDisplayView",creationParamsCodec: StandardMessageCodec(),);
  }
}
