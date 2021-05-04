//
//  TDSipPluginManager.h
//  td_sip_plugin
//
//  Created by 曾龙 on 2021/4/26.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN
@class TDSipPluginStreamHandler;
@interface TDSipPluginManager : NSObject
+ (instancetype)sharedInstance;
@property (nonatomic, strong) TDSipPluginStreamHandler* streamHandler;
@end

@interface TDSipPluginStreamHandler : NSObject<FlutterStreamHandler>
@property (nonatomic, strong) FlutterEventSink eventSink;
@end

NS_ASSUME_NONNULL_END
