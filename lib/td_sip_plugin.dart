import 'dart:async';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

enum TDSipLoginStatus {
  ///未登录
  TDSipLoginStatusNone,

  ///登录中
  TDSipLoginStatusProgress,

  ///登录成功
  TDSipLoginStatusOk,

  ///登录失败
  TDSipLoginStatusFailed
}

typedef void EventHandler(Map<String, dynamic> event);

class TdSipPlugin {
  static const MethodChannel _methodChannel =
      const MethodChannel('td_sip_plugin');
  static const MethodChannel _displayViewMethodChannel =
      const MethodChannel('TDDisplayView');
  static const EventChannel _eventChannel =
      const EventChannel("td_sip_plugin_stream");

  /// 注册Sip监听
  /// eventName:
  /// "callBusy">呼叫忙碌
  /// "didCallOut">成功呼出
  /// "didCallEnd">呼叫结束
  /// "streamsDidBeginRunning">收到视频音频流
  /// "didReceiveCallForID">收到呼叫 （"sipID">呼叫方的sipID）
  static addSipReceiver({@required EventHandler onEvent}) {
    _eventChannel.receiveBroadcastStream().listen((event) {
      Map map = new Map<String, dynamic>.from(event);
      if (map["eventName"] == "streamsDidBeginRunning") {
        _displayViewMethodChannel.invokeMethod("hideDisplayView");
      } else if (map["eventName"] == "didCallEnd") {
        _displayViewMethodChannel.invokeMethod("showDisplayView");
      }
      onEvent(map);
    });
  }

  /// 注册DisplayView监听，仅Android有效
  static addDisplayViewReceiver({@required EventHandler onEvent}) {
    _displayViewMethodChannel.setMethodCallHandler((call) async {
      switch (call.method) {
        case "setPlaceholder":
          onEvent({"eventName": "setPlaceholder"});
          break;
      }
    });
  }

  /// 登录Sip账号
  /// sipID         sip账号
  /// sipPassword   sip密码
  /// sipDomain     sip服务器地址
  /// sipPort       sip服务器端口号
  /// sipTransport  tcp or udp
  /// iceEnable     是否开启ice
  /// turnEnable    是否开启turn
  /// turnServer    turn服务器地址
  /// turnUser      turn账号
  /// turnPassword  turn密码
  static login({
    @required String sipID,
    @required String sipPassword,
    @required String sipDomain,
    @required String sipPort,
    String sipTransport = "tcp",
    bool iceEnable = false,
    bool turnEnable = false,
    String turnServer = "",
    String turnUser = "",
    String turnPassword = "",
  }) {
    _methodChannel.invokeMethod("login", {
      "sipID": sipID,
      "sipPassword": sipPassword,
      "sipDomain": sipDomain,
      "sipPort": sipPort,
      "sipTransport": sipTransport,
      "iceEnable": iceEnable,
      "turnEnable": turnEnable,
      "turnServer": turnServer,
      "turnUser": turnUser,
      "turnPassword": turnPassword,
    });
  }

  /// 退出登录
  static logout() {
    _methodChannel.invokeMethod("logout");
  }

  /// 获取登录状态
  static Future<TDSipLoginStatus> getLoginStatus() async {
    int index = await _methodChannel.invokeMethod("getLoginStatus");
    return TDSipLoginStatus.values[index];
  }

  /// 呼叫
  /// sipID 呼叫的sip账号
  static call(String sipID) {
    _methodChannel.invokeMethod("call", {"sipID": sipID});
  }

  /// 接听
  static answer() {
    _methodChannel.invokeMethod("answer");
  }

  /// 挂断
  static hangup() {
    _methodChannel.invokeMethod("hangup");
  }

  /// 切换音频播放设备（默认为扬声器）
  static switchSoundDevice() {
    _methodChannel.invokeMethod("switchSoundDevice");
  }

  /// 关闭麦克风
  static micOFF() {
    _methodChannel.invokeMethod("micOFF");
  }

  /// 打开麦克风
  static micON() {
    _methodChannel.invokeMethod("micON");
  }

  /// 设置背景图片
  static setPlaceholder(String placeholder) {
    _displayViewMethodChannel
        .invokeMethod("setPlaceholder", {"placeholder": placeholder});
  }
}
