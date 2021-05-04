//
//  TDDisplayView.h
//  td_sip_plugin
//
//  Created by 曾龙 on 2021/4/27.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface TDDisplayView : NSObject<FlutterPlatformView>

@end

@interface TDDisplayViewFactory : NSObject<FlutterPlatformViewFactory>
- (instancetype)initWithBinaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;
@end

NS_ASSUME_NONNULL_END
