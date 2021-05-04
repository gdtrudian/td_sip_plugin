package com.mz.td_sip_plugin.sip_tru;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.mz.td_sip_plugin.R;
import com.mz.td_sip_plugin.TdSipPlugin;

import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallParams;
import org.linphone.core.CallStats;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListener;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.ErrorInfo;
import org.linphone.core.Event;
import org.linphone.core.Factory;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.NatPolicy;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.Reason;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.TransportType;
import org.linphone.core.VersionUpdateCheckResult;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class SipTruMiniManager extends Service implements CoreListener {

    private static SipTruMiniManager mInstance;
    private String mCurrentAddress;
    private Factory lcFactory;
    private Core mSiptruCore;
    private Timer mTimer;
    private Context mContext;
    private AudioManager mAudioManager;
    private TdSipPlugin mSipPlugin;
    private RegistrationState mRegistrationState;
    public boolean isOpenAmplification = true;

    public static boolean isReady() {
        return mInstance != null;
    }

    public Core getLC() {
        return mSiptruCore;
    }

    public static SipTruMiniManager getInstance() {
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        lcFactory = Factory.instance();
        lcFactory.setDebugMode(true, "TDSip>>");
        mContext = this;

        try {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            SipTruMiniUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
            SipTruMiniUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
            SipTruMiniUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
            SipTruMiniUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
            mSiptruCore = lcFactory.createCore(basePath + "/.linphonerc", basePath + "/linphonerc", mContext);
            mSiptruCore = lcFactory.createCore(basePath + "/.linphonerc", basePath + "/linphonerc", mContext);
            mSiptruCore.addListener(this);
            String[] dnsServer = new String[]{"8.8.8.8"};
            mSiptruCore.setDnsServers(dnsServer);
            mSiptruCore.enableAdaptiveRateControl(false);
            mSiptruCore.start();
            mSiptruCore.setRootCa(basePath + "/rootca.pem");
            mSiptruCore.setRing(null);
            mSiptruCore.setRingback(null);
            TimerTask lTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (mSiptruCore != null) {
                            mSiptruCore.iterate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mTimer = new Timer("LinphoneMini scheduler");
            try {
                mTimer.schedule(lTask, 0, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mInstance = this;
            mSiptruCore.setNetworkReachable(true);
            mAudioManager = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSiptruCore = null;
        mTimer.cancel();
    }

    public void setProtocol(TdSipPlugin sipPlugin) {
        mSipPlugin = sipPlugin;
    }

    /**
     * 注册sip账号
     */
    public void registerSip(String sipID, String sipPassword, String sipDomain, String sipPort, String sipTransport, boolean iceEnable, boolean turnEnable, String turnServer, String turnUser, String turnPassword) {
        if (mSiptruCore == null) {
            return;
        }
        mSiptruCore.setInCallTimeout(65);
        for (ProxyConfig proxyConfig : mSiptruCore.getProxyConfigList()) {
            mSiptruCore.removeProxyConfig(proxyConfig);
        }
        if (iceEnable) {
            for (AuthInfo x : mSiptruCore.getAuthInfoList()) {
                mSiptruCore.removeAuthInfo(x);
            }
            setTurn(turnServer, turnUser, turnPassword);
        }

        AccountBuilder builder = new AccountBuilder(mSiptruCore)
                .setUsername(sipID)
                .setTransport(sipTransport=="udp"?TransportType.Udp:TransportType.Tcp)
                .setDomain(sipDomain + ":" + sipPort)
                .setHa1(null)
                .setUserid(sipID)
                .setExpires("120")
                .setDisplayName("")//显示名
                .setPassword(sipPassword);
        String prefix = null;
        builder.setAvpfEnabled(false);
        if (prefix != null) {
            builder.setPrefix(prefix);
        }
        String forcedProxy = "";//
        if (!TextUtils.isEmpty(forcedProxy)) {
            builder.setServerAddr(forcedProxy)
                    .setOutboundProxyEnabled(true);
        }
        try {
            builder.saveNewAccount(iceEnable);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出登录
     */
    public void logout() {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        mSiptruCore.terminateAllCalls();
        mSiptruCore.clearProxyConfig();
        mSiptruCore.clearAllAuthInfo();
    }

    public RegistrationState getLoginStatus() {
        if (mSiptruCore == null || mInstance == null || mRegistrationState == null) {
            return RegistrationState.None;
        }
        return mRegistrationState;
    }

    /**
     * 呼叫sip
     */
    public void callSip(String username) {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        ProxyConfig lpc = getLC().getDefaultProxyConfig();
        String host = lpc.getServerAddr().split(":")[1];
        Address address = mSiptruCore.interpretUrl(username + "@" + host);
        address.setDisplayName(username);
        CallParams params = mSiptruCore.createCallParams(null);
        params.enableVideo(true);
        mSiptruCore.inviteAddressWithParams(address, params);
    }

    /**
     * 挂断
     */
    public void hangUp() {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        mSiptruCore.terminateAllCalls();
    }

    /**
     * 打开扩音器
     */
    public void openAmplification(boolean isOpen) {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        if (mAudioManager == null) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(isOpen);
    }

    /**
     * 静音
     */
    public void micOFF() {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        mSiptruCore.enableMic(false);
    }

    /**
     * 取消静音
     */
    public void micON() {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        mSiptruCore.enableMic(true);
    }

    /**
     * 接通通话
     */
    public void acceptSip() {
        if (mSiptruCore == null || mInstance == null) {
            return;
        }
        Call currentCall = mSiptruCore.getCurrentCall();
        if (currentCall != null) {
            CallParams params = mSiptruCore.createCallParams(currentCall);
            params.enableVideo(true);
            currentCall.acceptWithParams(params);
        }
    }


    public void setTurn(String host, String username, String password) {
        if (getLC() == null) {
            return;
        }
        NatPolicy nat = getOrCreateNatPolicy();
        nat.setStunServer(host);
        nat.enableIce(true);
        nat.enableStun(true);
        nat.enableTurn(true);
        AuthInfo authInfo = getLC().findAuthInfo(null, nat.getStunServerUsername(), null);

        if (authInfo != null) {
            AuthInfo cloneAuthInfo = authInfo.clone();
            getLC().removeAuthInfo(authInfo);
            cloneAuthInfo.setUsername(username);
            cloneAuthInfo.setUserid(username);
            cloneAuthInfo.setPassword(password);
            getLC().addAuthInfo(cloneAuthInfo);
        } else {
            authInfo = Factory.instance().createAuthInfo(username, username, password, null, null, null);
            getLC().addAuthInfo(authInfo);
        }
        nat.setStunServerUsername(username);
        getLC().setNatPolicy(nat);
    }

    private NatPolicy getOrCreateNatPolicy() {
        if (mSiptruCore == null) return null;
        NatPolicy nat = mSiptruCore.getNatPolicy();
        if (nat == null) {
            nat = mSiptruCore.createNatPolicy();
        }
        return nat;
    }


    @Override
    public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState registrationState, String s) {
        mRegistrationState = registrationState;
        if (mSipPlugin != null) {
            mSipPlugin.registerStatusUpdate(registrationState);
        }
    }

    @Override
    public void onCallStateChanged(Core core, Call call, Call.State state, String s) {
        String stateStr = state.toString();
        if (TextUtils.isEmpty(mCurrentAddress)) {
            mCurrentAddress = call.getRemoteAddressAsString();
        }
        if (state == Call.State.IncomingReceived || state == Call.State.IncomingEarlyMedia) {
            call.enableCamera(false);
            if (!TextUtils.isEmpty(mCurrentAddress) && !call.getRemoteAddressAsString().equalsIgnoreCase(mCurrentAddress)) {
                ErrorInfo errorInfo = call.getErrorInfo();
                errorInfo.set("SIP", Reason.Forbidden, 403, "Another call is in progress", null);
                call.declineWithErrorInfo(errorInfo);
                mSiptruCore.terminateAllCalls();
                return;
            }
            String[] list = call.getRemoteAddressAsString().split("@");
            if (mSipPlugin != null) {
                mSipPlugin.callStatusUpdate("incoming", list[0]);
            }
        } else if (state == Call.State.OutgoingRinging) {
            call.enableCamera(false);
            if (mSipPlugin != null) {
                mSipPlugin.callStatusUpdate("outgoing", null);
            }
        } else if (state == Call.State.StreamsRunning) {
            openAmplification(true);
            mInstance.isOpenAmplification = true;
            if (mSipPlugin != null) {
                mSipPlugin.callStatusUpdate("streamsRunning", null);
            }
        } else if (s.contains("Another") ||
                s.contains("declined") ||
                s.contains("Busy")) {
            if (!call.getRemoteAddressAsString().equalsIgnoreCase(mCurrentAddress)) {
                return;
            }
            mCurrentAddress = "";
            if (mSipPlugin != null) {
                mSipPlugin.callStatusUpdate("busy", null);
            }
        } else if (state == Call.State.End) {
            if (!call.getRemoteAddressAsString().equalsIgnoreCase(mCurrentAddress)) {
                return;
            }
            mCurrentAddress = "";
            if (mSipPlugin != null) {
                mSipPlugin.callStatusUpdate("End", null);
            }
        } else if (state == Call.State.Error) {
            if (!call.getRemoteAddressAsString().equalsIgnoreCase(mCurrentAddress)) {
                return;
            }
            mCurrentAddress = "";
            if (mSipPlugin != null) {
                mSipPlugin.callStatusUpdate("End", null);
            }
        }
    }

    @Override
    public void onTransferStateChanged(Core core, Call call, Call.State state) {

    }

    @Override
    public void onFriendListCreated(Core core, FriendList friendList) {

    }

    @Override
    public void onSubscriptionStateChanged(Core core, Event event, SubscriptionState subscriptionState) {

    }

    @Override
    public void onCallLogUpdated(Core core, CallLog callLog) {

    }

    @Override
    public void onAuthenticationRequested(Core core, AuthInfo authInfo, AuthMethod authMethod) {

    }

    @Override
    public void onNotifyPresenceReceivedForUriOrTel(Core core, Friend friend, String s, PresenceModel presenceModel) {

    }

    @Override
    public void onChatRoomStateChanged(Core core, ChatRoom chatRoom, ChatRoom.State state) {

    }

    @Override
    public void onBuddyInfoUpdated(Core core, Friend friend) {

    }

    @Override
    public void onNetworkReachable(Core core, boolean b) {

    }

    @Override
    public void onNotifyReceived(Core core, Event event, String s, Content content) {

    }

    @Override
    public void onNewSubscriptionRequested(Core core, Friend friend, String s) {

    }

    @Override
    public void onNotifyPresenceReceived(Core core, Friend friend) {

    }

    @Override
    public void onEcCalibrationAudioInit(Core core) {

    }

    @Override
    public void onMessageReceived(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }

    @Override
    public void onEcCalibrationResult(Core core, EcCalibratorStatus ecCalibratorStatus, int i) {

    }

    @Override
    public void onSubscribeReceived(Core core, Event event, String s, Content content) {

    }

    @Override
    public void onInfoReceived(Core core, Call call, InfoMessage infoMessage) {

    }

    @Override
    public void onCallStatsUpdated(Core core, Call call, CallStats callStats) {

    }

    @Override
    public void onFriendListRemoved(Core core, FriendList friendList) {

    }

    @Override
    public void onReferReceived(Core core, String s) {

    }

    @Override
    public void onConfiguringStatus(Core core, ConfiguringState configuringState, String s) {

    }

    @Override
    public void onCallCreated(Core core, Call call) {

    }

    @Override
    public void onPublishStateChanged(Core core, Event event, PublishState publishState) {

    }

    @Override
    public void onCallEncryptionChanged(Core core, Call call, boolean b, String s) {

    }

    @Override
    public void onIsComposingReceived(Core core, ChatRoom chatRoom) {

    }

    @Override
    public void onMessageReceivedUnableDecrypt(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }

    @Override
    public void onLogCollectionUploadProgressIndication(Core core, int i, int i1) {

    }

    @Override
    public void onVersionUpdateCheckResultReceived(Core core, VersionUpdateCheckResult versionUpdateCheckResult, String s, String s1) {

    }

    @Override
    public void onEcCalibrationAudioUninit(Core core) {

    }

    @Override
    public void onGlobalStateChanged(Core core, GlobalState globalState, String s) {

    }

    @Override
    public void onLogCollectionUploadStateChanged(Core core, Core.LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void onDtmfReceived(Core core, Call call, int i) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent sIntent = new Intent("com.mz.td_sip_plugin_init_success");
        sendBroadcast(sIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    public static class AccountBuilder {
        private Core lc;
        private String tempUsername;
        private String tempDisplayName;
        private String tempUserId;
        private String tempPassword;
        private String tempHa1;
        private String tempDomain;
        private String tempProxy;
        private String tempPrefix;
        private boolean tempOutboundProxy;
        private String tempExpire;
        private TransportType tempTransport;
        private boolean tempAvpfEnabled = false;
        private int tempAvpfRRInterval = 0;
        private boolean tempQualityReportingEnabled = false;
        private int tempQualityReportingInterval = 0;
        private boolean tempEnabled = true;
        private boolean tempNoDefault = false;


        public AccountBuilder(Core lc) {
            this.lc = lc;
        }

        public AccountBuilder setTransport(TransportType transport) {
            tempTransport = transport;
            return this;
        }

        public AccountBuilder setUsername(String username) {
            tempUsername = username;
            return this;
        }

        public AccountBuilder setDisplayName(String displayName) {
            tempDisplayName = displayName;
            return this;
        }

        public AccountBuilder setPassword(String password) {
            tempPassword = password;
            return this;
        }

        public AccountBuilder setHa1(String ha1) {
            tempHa1 = ha1;
            return this;
        }

        public AccountBuilder setDomain(String domain) {
            tempDomain = domain;
            return this;
        }

        public AccountBuilder setServerAddr(String proxy) {
            tempProxy = proxy;
            return this;
        }

        public AccountBuilder setOutboundProxyEnabled(boolean enabled) {
            tempOutboundProxy = enabled;
            return this;
        }

        public AccountBuilder setExpires(String expire) {
            tempExpire = expire;
            return this;
        }

        public AccountBuilder setUserid(String userId) {
            tempUserId = userId;
            return this;
        }

        public AccountBuilder setAvpfEnabled(boolean enable) {
            tempAvpfEnabled = enable;
            return this;
        }


        public AccountBuilder setPrefix(String prefix) {
            tempPrefix = prefix;
            return this;
        }


        public void saveNewAccount(boolean isOpenIce) throws CoreException {
            if (tempUsername == null || tempUsername.length() < 1 || tempDomain == null || tempDomain.length() < 1) {
                return;
            }
            String identity = "sip:" + tempUsername + "@" + tempDomain;
            String proxy = "sip:";
            if (tempProxy == null) {
                proxy += tempDomain;
            } else {
                if (!tempProxy.startsWith("sip:") && !tempProxy.startsWith("<sip:")
                        && !tempProxy.startsWith("sips:") && !tempProxy.startsWith("<sips:")) {
                    proxy += tempProxy;
                } else {
                    proxy = tempProxy;
                }
            }
            Address proxyAddr = Factory.instance().createAddress(proxy);
            Address identityAddr = Factory.instance().createAddress(identity);
            if (proxyAddr == null || identityAddr == null) {
                throw new CoreException("Proxy or Identity address is null !");
            }
            if (tempDisplayName != null) {
                identityAddr.setDisplayName(tempDisplayName);
            }
            if (tempTransport != null) {
                proxyAddr.setTransport(tempTransport);
            }
            String route = tempOutboundProxy ? proxyAddr.asStringUriOnly() : null;
            ProxyConfig prxCfg = lc.createProxyConfig();
            prxCfg.setIdentityAddress(identityAddr);
            prxCfg.setServerAddr(proxyAddr.asStringUriOnly());
            prxCfg.setRoute(route);
            prxCfg.enableRegister(tempEnabled);
            if (tempExpire != null) {
                prxCfg.setExpires(Integer.parseInt(tempExpire));
            }
            prxCfg.setAvpfMode(tempAvpfEnabled ? AVPFMode.Enabled : AVPFMode.Disabled);
            prxCfg.setAvpfRrInterval(tempAvpfRRInterval);
            prxCfg.enableQualityReporting(tempQualityReportingEnabled);
            prxCfg.setQualityReportingInterval(tempQualityReportingInterval);
            if (tempPrefix != null) {
                prxCfg.setDialPrefix(tempPrefix);
            }
            AuthInfo authInfo = Factory.instance().createAuthInfo(tempUsername, tempUserId, tempPassword, tempHa1, "", tempDomain);
            lc.addProxyConfig(prxCfg);
            lc.addAuthInfo(authInfo);
            if (!isOpenIce) {
                prxCfg.edit();
                NatPolicy natPolicy = getInstance().getLC().createNatPolicy();
                natPolicy.enableTurn(false);
                natPolicy.enableStun(false);
                natPolicy.enableIce(false);
                prxCfg.setNatPolicy(natPolicy);
                prxCfg.done();
            }
            if (!tempNoDefault) {
                lc.setDefaultProxyConfig(prxCfg);
            }
        }
    }
}
