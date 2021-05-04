package com.mz.td_sip_plugin.sip_tru;
import android.content.Context;

import org.linphone.core.Call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SipTruMiniUtils {
    public static void copyIfNotExist(Context context, int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(context, ressourceId, lFileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = context.openFileOutput (target, 0);
        InputStream lInputStream = context.getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while (( readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff,0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
    
    public static boolean isCallEstablished(Call call) {
        if (call == null) {
            return false;
        }

        Call.State state = call.getState();

        return isCallRunning(call)
                || state == Call.State.Paused
                || state == Call.State.PausedByRemote
                || state == Call.State.Pausing;
    }

    public static boolean isCallRunning(Call call) {
        if (call == null) {
            return false;
        }

        Call.State state = call.getState();

        return state == Call.State.Connected
                || state == Call.State.Updating
                || state == Call.State.UpdatedByRemote
                || state == Call.State.StreamsRunning
                || state == Call.State.Resuming;
    }
}
