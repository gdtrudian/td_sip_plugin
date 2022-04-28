#import "TdSipPlugin.h"
#import <TDSip/TDSipManager.h>
#import "TDSipPluginManager.h"
#import "TDDisplayView.h"

@interface TdSipPlugin ()<TDSipManagerDeleagte>

@end

@implementation TdSipPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"td_sip_plugin"
            binaryMessenger:[registrar messenger]];
  TdSipPlugin* instance = [[TdSipPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];

  [registrar registerViewFactory:[[TDDisplayViewFactory alloc] initWithBinaryMessenger:[registrar messenger]] withId:@"TDDisplayView"];
    
  [[TDSipManager shareInstance] setDelegate:instance];
  [[TDSipManager shareInstance] setLoginStatusUpdate:^(TDSipLoginStatus status) {
      if ([[TDSipPluginManager sharedInstance] streamHandler].eventSink) {
          [[TDSipPluginManager sharedInstance] streamHandler].eventSink(@{@"eventName":@"loginStatus",@"loginStatus":[NSNumber numberWithInteger:status]});
      }
  }];

  FlutterEventChannel *registerEventChannel = [FlutterEventChannel eventChannelWithName:@"td_sip_plugin_stream" binaryMessenger:[registrar messenger]];
  [registerEventChannel setStreamHandler:[[TDSipPluginManager sharedInstance] streamHandler]];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"login" isEqualToString:call.method]) {
      NSString *sipID = call.arguments[@"sipID"];
      NSString *sipPassword = call.arguments[@"sipPassword"];
      NSString *sipDomain = call.arguments[@"sipDomain"];
      NSString *sipPort = call.arguments[@"sipPort"];
      NSString *sipTransport = call.arguments[@"sipTransport"];
      BOOL iceEnable = [call.arguments[@"iceEnable"] boolValue];
      BOOL turnEnable = [call.arguments[@"turnEnable"] boolValue];
      NSString *turnServer = call.arguments[@"turnServer"];
      NSString *turnUser = call.arguments[@"turnUser"];
      NSString *turnPassword = call.arguments[@"turnPassword"];
      [TDSipManager loginWithSipID:sipID sipPassword:sipPassword sipDomain:sipDomain sipPort:sipPort sipTransport:sipTransport iceEnable:iceEnable turnEnable:turnEnable turnServer:turnServer turnUser:turnUser turnPassword:turnPassword];
  } else if ([@"logout" isEqualToString:call.method]) {
      [TDSipManager logout];
  } else if ([@"getLoginStatus" isEqualToString:call.method]) {
      result([NSNumber numberWithInteger:[TDSipManager getLoginStatus]]);
  } else if ([@"call" isEqualToString:call.method]) {
      NSString *sipID = call.arguments[@"sipID"];
      [TDSipManager call:sipID];
  } else if ([@"answer" isEqualToString:call.method]) {
      [TDSipManager answer];
  } else if ([@"hangup" isEqualToString:call.method]) {
      [TDSipManager hangup];
  } else if ([@"switchToLoudspeaker" isEqualToString:call.method]) {
      [TDSipManager switchToLoudspeaker];
  } else if ([@"switchToEarphone" isEqualToString:call.method]) {
      [TDSipManager switchToEarphone];
  } else if ([@"micOFF" isEqualToString:call.method]) {
      [TDSipManager micOFF];
  } else if ([@"micON" isEqualToString:call.method]) {
      [TDSipManager micON];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

#pragma mark - TDSipManagerDeleagte

- (void)callBusy {
    [[TDSipPluginManager sharedInstance] streamHandler].eventSink(@{@"eventName":@"callBusy"});
}

- (void)didCallOut {
    [[TDSipPluginManager sharedInstance] streamHandler].eventSink(@{@"eventName":@"didCallOut"});
}

- (void)didCallEnd {
    [[TDSipPluginManager sharedInstance] streamHandler].eventSink(@{@"eventName":@"didCallEnd"});
}

- (void)didReceiveCallForID:(NSString *)ID {
    [[TDSipPluginManager sharedInstance] streamHandler].eventSink(@{@"eventName":@"didReceiveCallForID",@"sipID":ID});
}

- (void)streamsDidBeginRunning {
    [[TDSipPluginManager sharedInstance] streamHandler].eventSink(@{@"eventName":@"streamsDidBeginRunning"});
}

@end
