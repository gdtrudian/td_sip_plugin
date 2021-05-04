package com.mz.td_sip_plugin.sip_tru;
import org.linphone.core.RegistrationState;

public interface SipTruMiniManagerListener {
    void registerStatusUpdate(RegistrationState state);
    void callStatusUpdate(String status, String sipID);
}
