package com.mz.td_sip_plugin;

import android.content.Context;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class TdDisplayViewFactory extends PlatformViewFactory {

    private final BinaryMessenger messenger;

    public TdDisplayViewFactory(BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        return new TdDisplayView(context, messenger, viewId, args);
    }
}
