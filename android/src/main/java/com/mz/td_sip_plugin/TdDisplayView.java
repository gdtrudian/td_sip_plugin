package com.mz.td_sip_plugin;

import android.content.Context;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mz.td_sip_plugin.sip_tru.SipTruMiniManager;

import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.display.GL2JNIView;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformView;

public class TdDisplayView implements PlatformView {

    private final FrameLayout mFrameLayout;
    private final GL2JNIView mRenderingView;
    private AndroidVideoWindowImpl mAndroidVideoWindow;

    TdDisplayView(Context context, BinaryMessenger messenger, int viewId, Object args) {

        mFrameLayout = new FrameLayout(context);
        mFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        mRenderingView = new GL2JNIView(context);
        mRenderingView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mFrameLayout.addView(mRenderingView);

        mAndroidVideoWindow = new AndroidVideoWindowImpl(mRenderingView, null, new AndroidVideoWindowImpl.VideoWindowListener() {
            @Override
            public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl androidVideoWindow, SurfaceView surfaceView) {
                SipTruMiniManager.getInstance().getLC().setNativeVideoWindowId(androidVideoWindow);
            }

            @Override
            public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl androidVideoWindow) {
                SipTruMiniManager.getInstance().getLC().setNativeVideoWindowId(null);
            }

            @Override
            public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl androidVideoWindow, SurfaceView surfaceView) {
            }

            @Override
            public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl androidVideoWindow) {
            }
        });

    }

    @Override
    public View getView() {
        return mFrameLayout;
    }

    @Override
    public void dispose() {
        if (mAndroidVideoWindow != null) {
            mAndroidVideoWindow.release();
            mAndroidVideoWindow = null;
        }
    }
}
