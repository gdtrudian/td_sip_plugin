//
//  TDSipPluginManager.m
//  td_sip_plugin
//
//  Created by 曾龙 on 2021/4/26.
//

#import "TDSipPluginManager.h"

@implementation TDSipPluginManager
+ (instancetype)sharedInstance {
    static dispatch_once_t onceToken;
    static TDSipPluginManager *manager = nil;
    dispatch_once(&onceToken, ^{
        manager = [[TDSipPluginManager alloc] init];
        TDSipPluginStreamHandler *streamHandler = [[TDSipPluginStreamHandler alloc] init];
        manager.streamHandler = streamHandler;
    });
    return manager;
}
@end

@implementation TDSipPluginStreamHandler

- (FlutterError *)onListenWithArguments:(id)arguments eventSink:(FlutterEventSink)events {
    self.eventSink = events;
    return nil;
}

- (FlutterError *)onCancelWithArguments:(id)arguments {
    return nil;
}

@end
