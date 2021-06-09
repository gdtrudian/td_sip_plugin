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

abstract class TdSipObserver {
  /// 登录状态更新
  void tdSipLoginStatus(TDSipLoginStatus status) {}

  /// 呼叫忙碌
  void tdSipCallBusy() {}

  /// 成功呼出
  void tdSipDidCallOut() {}

  /// 呼叫或通话结束
  void tdSipDidCallEnd() {}

  /// 收到视频音频流
  void tdSipStreamsDidBeginRunning() {}

  /// 收到呼叫（"sipID">呼叫方的sipID）
  void tdSipDidReceiveCallForID(String sipID) {}
}

class TdSipPlugin {
  static const MethodChannel _methodChannel =
      const MethodChannel('td_sip_plugin');
  static const EventChannel _eventChannel =
      const EventChannel("td_sip_plugin_stream");
  static List<TdSipObserver> _observerList = [];

  static initial() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      Future.delayed(Duration(seconds: 2), () {
        _methodChannel.invokeMethod("initial");
      });
    }
    _eventChannel.receiveBroadcastStream().listen((event) {
      Map map = new Map<String, dynamic>.from(event);
      switch (map["eventName"]) {
        case "loginStatus":
          _observerList.forEach((TdSipObserver observer) {
            observer
                .tdSipLoginStatus(TDSipLoginStatus.values[map["loginStatus"]]);
          });
          break;
        case "didReceiveCallForID":
          _observerList.forEach((TdSipObserver observer) {
            observer.tdSipDidReceiveCallForID(map["sipID"]);
          });
          break;
        case "didCallOut":
          _observerList.forEach((TdSipObserver observer) {
            observer.tdSipDidCallOut();
          });
          break;
        case "didCallEnd":
          _observerList.forEach((TdSipObserver observer) {
            observer.tdSipDidCallEnd();
          });
          break;
        case "callBusy":
          _observerList.forEach((TdSipObserver observer) {
            observer.tdSipCallBusy();
          });
          break;
        case "streamsDidBeginRunning":
          _observerList.forEach((TdSipObserver observer) {
            observer.tdSipStreamsDidBeginRunning();
          });
          break;
      }
    });
  }

  /// 注册Sip监听
  static addSipObserver(TdSipObserver observer) {
    _observerList.add(observer);
  }

  /// 移除监听
  static removeSipObserver(TdSipObserver observer) {
    _observerList.remove(observer);
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

  /// 跳转息屏显示呼叫页面，仅Android可用
  static showSipPage() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      _methodChannel.invokeMethod("showSipPage");
    }
  }
}
