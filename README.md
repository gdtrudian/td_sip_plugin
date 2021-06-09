# td_sip_plugin

A Flutter plugin for sip belong Trudian.

## Usage

To use the plugin ,add `td_sip_plugin` as a dependency in your pubspec.yaml

## Android
use it in Android, you have to add this permission in your AndroidManifest.xml:
```
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

## iOS
And to user it in iOS, you have to add this permission in Info.plist :
```
Privacy - Microphone Usage Description
```

### Example

``` dart
    //setup the video display view(设置门口机视频显示)
    Container(
      width: 200,
      height: 120,
      child: TDDisplayView(),
    )
    
    // 添加sip监听
    TdSipPlugin.addSipObserver(this);

    // 移除sip监听
    TdSipPlugin.removeSipObserver(this);

    //sip login(登录sip账号)
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
    
    // sip call(sip呼叫)
    // attention:check the microphone permission before sip call
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
    
    // sip logout(sip退出登录)
    TdSipPlugin.logout();
    
    //sip hangup(sip挂断)
    TdSipPlugin.hangup();
    
    //sip answer(sip接听)
    TdSipPlugin.answer();
    
    // sip login status(获取sip登录状态)
    TDSipLoginStatus status = await TdSipPlugin.getLoginStatus();
```

Please see the example of this plugin for a full example

[See the plugin in github](https://git.trudian.com/Trudian/td_sip_plugin.git)

