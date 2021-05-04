import 'package:flutter/material.dart';
import 'package:td_sip_plugin/td_sip_plugin.dart';
import 'package:td_sip_plugin/TDDisplayView.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _loginStatus = "";
  String _callStatus = "";

  @override
  void initState() {
    super.initState();
    _getLoginStatus();
    TdSipPlugin.addSipReceiver(onEvent: (event) {
      print(event.toString());
      if (event["eventName"] == "loginStatus") {
        setState(() {
          _loginStatus =
              "${TDSipLoginStatus.values[event["loginStatus"]]}";
        });
      } else {
        setState(() {
          _callStatus = event["eventName"];
        });
      }
    });
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
      if(newStatus.isGranted) {
        TdSipPlugin.call("1907556514130605");
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
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
                      sipID: "1100000004",
                      sipPassword: "2ebdeb8b65d320b2",
                      sipDomain: "47.106.186.8",
                      sipPort: "8060",
                      turnEnable: false,
                      iceEnable: false,
                      turnServer: "",
                      turnUser: "",
                      turnPassword: "");
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
              Text(_callStatus,),
              Container(
                width: 200,
                height: 120,
                child: TDDisplayView(
                  placeholder: "images/video.png",
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}
