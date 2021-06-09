import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class TDDisplayView extends StatefulWidget {
  @override
  _TDDisplayViewState createState() => _TDDisplayViewState();
}

class _TDDisplayViewState extends State<TDDisplayView> {
  @override
  Widget build(BuildContext context) {
    return defaultTargetPlatform == TargetPlatform.iOS
        ? UiKitView(
            viewType: "TDDisplayView",
            creationParamsCodec: StandardMessageCodec(),
          )
        : AndroidView(
            viewType: "TDDisplayView",
            creationParamsCodec: StandardMessageCodec(),
          );
  }
}
