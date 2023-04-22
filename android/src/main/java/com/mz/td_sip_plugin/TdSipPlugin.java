package com.mz.td_sip_plugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.mz.td_sip_plugin.sip_tru.SipTruMiniManager;
import com.mz.td_sip_plugin.sip_tru.SipTruMiniManagerListener;

import org.linphone.core.RegistrationState;

import java.util.HashMap;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;


/**
 * TdSipPlugin
 */
public class TdSipPlugin extends BroadcastReceiver implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, SipTruMiniManagerListener, ActivityAware {

    private MethodChannel methodChannel;
    private EventChannel eventChannel;
    private EventChannel.EventSink mEvents;
    private Activity mActivity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "td_sip_plugin");
        methodChannel.setMethodCallHandler(this);

        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "td_sip_plugin_stream");
        eventChannel.setStreamHandler(this);

        flutterPluginBinding.getPlatformViewRegistry().registerViewFactory("TDDisplayView", new TdDisplayViewFactory(flutterPluginBinding.getBinaryMessenger()));

        Context application = flutterPluginBinding.getApplicationContext();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.mz.td_sip_plugin_init_success");
        application.registerReceiver(this, intentFilter);

        final Intent i = new Intent(application, SipTruMiniManager.class);
        application.startService(i);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("initial")) {
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().initial();
            }
        } else if (call.method.equals("login")) {
            HashMap<String, Object> map = (HashMap) call.arguments;
            String sipID = (String) map.get("sipID");
            String sipPassword = (String) map.get("sipPassword");
            String sipDomain = (String) map.get("sipDomain");
            String sipPort = (String) map.get("sipPort");
            String sipTransport = (String) map.get("sipTransport");
            boolean iceEnable = (boolean) map.get("iceEnable");
            boolean turnEnable = (boolean) map.get("turnEnable");
            String turnServer = (String) map.get("turnServer");
            String turnUser = (String) map.get("turnUser");
            String turnPassword = (String) map.get("turnPassword");

            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().registerSip(sipID, sipPassword, sipDomain, sipPort, sipTransport, iceEnable, turnEnable, turnServer, turnUser, turnPassword);
            }
        } else if (call.method.equals("logout")) {
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().logout();
            }
        } else if (call.method.equals("getLoginStatus")) {
            if (SipTruMiniManager.isReady()) {
                RegistrationState state = SipTruMiniManager.getInstance().getLoginStatus();
                int reg = 0;
                if (state.toInt() < 3) {
                    reg = state.toInt();
                } else if (state == RegistrationState.Failed) {
                    reg = 3;
                }
                result.success(reg);
            }
        } else if (call.method.equals("call")) {
            RegistrationState state = SipTruMiniManager.getInstance().getLoginStatus();
            if (state != RegistrationState.Ok) {
                return;
            }
            HashMap<String, Object> map = (HashMap) call.arguments;
            String sipID = (String) map.get("sipID");
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().callSip(sipID);
            }
        } else if (call.method.equals("answer")) {
            RegistrationState state = SipTruMiniManager.getInstance().getLoginStatus();
            if (state != RegistrationState.Ok) {
                return;
            }
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().acceptSip();
            }
        } else if (call.method.equals("hangup")) {
            RegistrationState state = SipTruMiniManager.getInstance().getLoginStatus();
            if (state != RegistrationState.Ok) {
                return;
            }
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().hangUp();
            }
        } else if (call.method.equals("switchToLoudspeaker")) {
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().openAmplification(true);
            }
        } else if (call.method.equals("switchToEarphone")) {
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().openAmplification(false);
            }
        } else if (call.method.equals("micOFF")) {
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().micOFF();
            }
        } else if (call.method.equals("micON")) {
            if (SipTruMiniManager.isReady()) {
                SipTruMiniManager.getInstance().micON();
            }
        } else if (call.method.equals("showSipPage")) {
            if (mActivity != null) {
                Intent intent = FlutterActivity.withNewEngine().initialRoute("/td_sip_page").build(mActivity.getApplicationContext());
                intent.putExtra("isSipActivity", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mActivity.startActivity(intent);
            }
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
    }

    @Override
    public void registerStatusUpdate(RegistrationState state) {
        if (mEvents != null) {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("eventName", "loginStatus");
            if (state.toInt() < 3) {
                map.put("loginStatus", state.toInt());
            } else if (state == RegistrationState.Failed) {
                map.put("loginStatus", 3);
            } else {
                map.put("loginStatus", 0);
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mEvents.success(map);
                }
            });
        }
    }

    @Override
    public void callStatusUpdate(String status, final String sipID) {
        if (mEvents != null) {
            final HashMap<String, String> map = new HashMap<>();
            switch (status) {
                case "busy":
                    map.put("eventName", "callBusy");
                    break;
                case "outgoing":
                    map.put("eventName", "didCallOut");
                    break;
                case "End":
                    map.put("eventName", "didCallEnd");
                    break;
                case "incoming":
                    map.put("eventName", "didReceiveCallForID");
                    map.put("sipID", sipID);
                    break;
                case "streamsRunning":
                    map.put("eventName", "streamsDidBeginRunning");
                    break;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mEvents.success(map);
                }
            });
        }
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        mEvents = events;
    }

    @Override
    public void onCancel(Object arguments) {
        mEvents = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SipTruMiniManager.isReady()) {
            SipTruMiniManager.getInstance().setProtocol(this);
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        mActivity = binding.getActivity();
        boolean isSipActivity = mActivity.getIntent().getBooleanExtra("isSipActivity", false);
        if (isSipActivity) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏显示
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕不息屏
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

}

