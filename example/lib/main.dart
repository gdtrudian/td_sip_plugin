import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:mz_back_plugin/mz_back_plugin.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:td_sip_plugin/TDDisplayView.dart';
import 'package:td_sip_plugin/td_sip_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();

    /// 初始化云对讲插件
    TdSipPlugin.initial();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      initialRoute: "/",
      routes: <String, WidgetBuilder>{
        "/": (BuildContext context) => HomePage(),
        "/td_sip_page": (BuildContext context) => SipPage(),
      },
    );
  }
}

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage>
    with WidgetsBindingObserver, TdSipObserver {
  String _loginStatus = "";
  String _callStatus = "";
  bool _isPaused = false; //判断是都处于后台

  @override
  void initState() {
    super.initState();
    _getLoginStatus();

    WidgetsBinding.instance.addObserver(this);
    TdSipPlugin.addSipObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);

    /// 移除监听
    TdSipPlugin.removeSipObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    _isPaused = state == AppLifecycleState.paused;
  }

  @override
  void tdSipLoginStatus(TDSipLoginStatus status) {
    super.tdSipLoginStatus(status);
    setState(() {
      _loginStatus = "$status";
    });
  }

  @override
  void tdSipDidCallOut() {
    super.tdSipDidCallOut();
    Navigator.of(context).pushNamed("/td_sip_page");
  }

  @override
  void tdSipDidCallEnd() {
    super.tdSipDidCallEnd();
    Navigator.of(context).pop();
  }

  @override
  void tdSipDidReceiveCallForID(String sipID) {
    super.tdSipDidReceiveCallForID(sipID);

    /// 设置呼叫页面息屏显示后，只有iOS需要做页面跳转处理，Android已在原生底层处理，只需要实现路由为的页面即可
    /// ⚠️ 路由"/td_sip_page"为固定的呼叫页面路由
    if (defaultTargetPlatform == TargetPlatform.android && _isPaused) {
      /// 这里可以本地存储相关呼叫信息，然后在SipPage里面去获取
      /// 比如shared_preferences
      TdSipPlugin.showSipPage();
    } else {
      Navigator.of(context)
          .pushNamed("/td_sip_page", arguments: {"sipID": sipID});
    }
  }

  void _getLoginStatus() async {
    TDSipLoginStatus status = await TdSipPlugin.getLoginStatus();
    setState(() {
      _loginStatus = "$status";
    });
  }

  void _checkPermission() async {
    Permission permission = Permission.microphone;
    PermissionStatus status = await permission.status;
    if (status.isGranted) {
      TdSipPlugin.call("1907556514130605");
    } else if (status.isPermanentlyDenied) {
      ///用户点击了 拒绝且不再提示
    } else {
      PermissionStatus newStatus = await permission.request();
      if (newStatus.isGranted) {
        TdSipPlugin.call("1907556514130605");
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text(_loginStatus),
              SizedBox(
                height: 20,
              ),
              ElevatedButton(
                child: Text("登录"),
                onPressed: () {
                  TdSipPlugin.login(
                      sipID: "110000004",
                      sipPassword: "285f758dff64a1d8",
                      sipDomain: "47.106.186.8",
                      sipPort: "8060");
                },
              ),
              ElevatedButton(
                child: Text("退出登录"),
                onPressed: () {
                  TdSipPlugin.logout();
                },
              ),
              ElevatedButton(
                child: Text("呼叫"),
                onPressed: () {
                  _checkPermission();
                },
              ),
              Text(
                _callStatus,
              ),
            ],
          ),
        ),
      ),
      onWillPop: _onWillPop,
    );
  }

  Future<bool> _onWillPop() {
    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return Future.value(true);
    }

    /// 处理Android物理返回桌面后app销毁的问题
    MzBackPlugin.navigateToSystemHome();
    return Future.value(false);
  }
}

class SipPage extends StatefulWidget {
  @override
  _SipPageState createState() => _SipPageState();
}

class _SipPageState extends State<SipPage> with TdSipObserver {
  bool _showPlaceholder = true;

  @override
  void initState() {
    super.initState();
    TdSipPlugin.addSipObserver(this);
  }

  @override
  void dispose() {
    TdSipPlugin.removeSipObserver(this);
    super.dispose();
  }

  @override
  void tdSipStreamsDidBeginRunning() {
    super.tdSipStreamsDidBeginRunning();
    setState(() {
      _showPlaceholder = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    print(ModalRoute.of(context).settings.arguments);
    Size size = MediaQuery.of(context).size;
    return Scaffold(
      body: Container(
        width: size.width,
        height: size.height,
        color: Colors.brown,
        child: Center(
          child: Column(
            children: [
              SizedBox(
                height: 200,
              ),
              Container(
                width: 200,
                height: 120,
                child: Stack(
                  children: [
                    TDDisplayView(),
                    Visibility(
                        visible: _showPlaceholder,
                        child: Image.asset(
                          "images/video.png",
                          width: 200,
                          height: 120,
                          fit: BoxFit.cover,
                        ))
                  ],
                ),
              ),
              ElevatedButton(
                child: Text("挂断"),
                onPressed: () {
                  TdSipPlugin.hangup();
                },
              ),
              ElevatedButton(
                child: Text("接听"),
                onPressed: () {
                  TdSipPlugin.answer();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
