//
//  TDDisplayView.m
//  td_sip_plugin
//
//  Created by 曾龙 on 2021/4/27.
//

#import "TDDisplayView.h"
#import <TDSip/TDSipManager.h>

@interface TDDisplayView ()
@property (nonatomic, strong) UIImageView *displayView;
@property (nonatomic, assign) CGRect frame;
@end

@implementation TDDisplayView

- (UIImageView *)displayView {
    if (_displayView == nil) {
        _displayView = [[UIImageView alloc] initWithFrame:self.frame];
        _displayView.backgroundColor = [UIColor blackColor];
        [TDSipManager setVideoView:_displayView];
    }
    return _displayView;
}

- (instancetype)initWithFrame:(CGRect)frame viewId:(int64_t)viewId args:(NSDictionary *)args binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
    if (self) {
        self.frame = frame;
    }
    return self;
}

- (UIView *)view {
    return self.displayView;
}

@end

@implementation TDDisplayViewFactory {
    NSObject<FlutterBinaryMessenger> *_messenger;
}

- (instancetype)initWithBinaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
        if (self) {
            _messenger = messenger;
        }
        return self;
}

- (NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args {
    return [[TDDisplayView alloc] initWithFrame:frame viewId:viewId args:args binaryMessenger:_messenger];
}

- (NSObject<FlutterMessageCodec> *)createArgsCodec {
    return  [FlutterStandardMessageCodec sharedInstance];
}

@end
