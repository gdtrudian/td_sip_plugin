package com.mz.td_sip_plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.mz.td_sip_plugin.sip_tru.SipTruMiniManager;

import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.display.GL2JNIView;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class TdDisplayView implements PlatformView, MethodChannel.MethodCallHandler {

    private final FrameLayout mFrameLayout;
    private final ImageView mImageView;
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

        mImageView = new ImageView(context);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mImageView.setVisibility(View.INVISIBLE);
        mFrameLayout.addView(mImageView);

        MethodChannel methodChannel = new MethodChannel(messenger, "TDDisplayView");
        methodChannel.setMethodCallHandler(this);
        methodChannel.invokeMethod("setPlaceholder", null);

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

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if (call.method.equals("setPlaceholder")) {
            mImageView.setVisibility(View.VISIBLE);
            HashMap<String, String> map = (HashMap) call.arguments;
            String placeholder = map.get("placeholder");
            byte[] decodeString = Base64.decode(placeholder, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodeString,0, decodeString.length);
            mImageView.setImageBitmap(bitmap);
        } else if (call.method.equals("hideDisplayView")) {
            mImageView.setVisibility(View.INVISIBLE);
        } else if (call.method.equals("showDisplayView")) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }
}
