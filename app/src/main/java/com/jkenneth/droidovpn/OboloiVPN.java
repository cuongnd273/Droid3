package com.jkenneth.droidovpn;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import com.apero.openvpn.LaunchVPN;
import com.apero.openvpn.VpnProfile;
import com.apero.openvpn.core.App;
import com.apero.openvpn.core.ConnectionStatus;
import com.apero.openvpn.core.IOpenVPNServiceInternal;
import com.apero.openvpn.core.OpenVPNService;
import com.apero.openvpn.core.ProfileManager;
import com.apero.openvpn.core.VpnStatus;


public class OboloiVPN implements VpnStatus.ByteCountListener, VpnStatus.StateListener {

    //    final OboloiVPN ourInstance = new OboloiVPN();
    private static IOpenVPNServiceInternal mService;
    private ProfileAsync profileAsync;

    private boolean profileStatus;

    private Activity activity;
    private Context context;
    private OnVPNStatusChangeListener listener;

    private boolean value;

    public void setOnVPNStatusChangeListener(OnVPNStatusChangeListener listener) {
        this.listener = listener;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    public OboloiVPN(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;

    }


    public void launchVPN(String url) {

        if (!App.isStart) {
            DataCleanManager.cleanApplicationData(context);
            setProfileLoadStatus(false);
            profileAsync = new ProfileAsync(context, new ProfileAsync.OnProfileLoadListener() {
                @Override
                public void onProfileLoadSuccess() {
                    setProfileLoadStatus(true);
                }

                @Override
                public void onProfileLoadFailed(String msg) {

                    Toast.makeText(context, activity.getString(R.string.init_fail) + msg, Toast.LENGTH_SHORT).show();
                }
            }, url);
            profileAsync.execute();
        }

    }

    public void launchVPN(File file) {

        if (!App.isStart) {
            DataCleanManager.cleanApplicationData(context);
            setProfileLoadStatus(false);
            profileAsync = new ProfileAsync(context, new ProfileAsync.OnProfileLoadListener() {
                @Override
                public void onProfileLoadSuccess() {
                    setProfileLoadStatus(true);
                }

                @Override
                public void onProfileLoadFailed(String msg) {
                    Log.e(TAG, "onProfileLoadFailed: " + msg);
                    Toast.makeText(context, activity.getString(R.string.init_fail) + msg, Toast.LENGTH_SHORT).show();
                }
            }, file);
            profileAsync.execute();
        } else {
            stopVPN();

        }

    }


    public void init() {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (!App.isStart) {
                    startVPN();
                    App.isStart = true;

                } else {
                    stopVPN();

                }
            }
        };
        r.run();
    }


    public void onStop() {
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
    }

    public void onResume() {
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
        Intent intent = new Intent(activity, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onPause() {
        activity.unbindService(mConnection);
    }

    public void cleanup() {
        if (profileAsync != null && !profileAsync.isCancelled()) {
            profileAsync.cancel(true);
        }
    }

    private void startVPN() {
        try {
            ProfileManager pm = ProfileManager.getInstance(context);
            VpnProfile profile = pm.getProfileByName(Build.MODEL);
            startVPNConnection(profile);
        } catch (Exception ex) {
            App.isStart = false;

        }
    }

    public void stopVPN() {
        App.isStart = false;
        stopVPNConnection();
        setVPNStatus(false);
        //btnConnect.setText(getString(R.string.connect));

    }

    public boolean getVPNStatus() {
        return value;
    }

    public boolean getProfileStatus() {
        return profileStatus;
    }


    private void setVPNStatus(boolean value) {
        this.value = value;
        if (listener != null) {
            listener.onVPNStatusChanged(value);
        }
    }

    private void setProfileLoadStatus(boolean profileStatus) {
        this.profileStatus = profileStatus;

        if (listener != null) {
            listener.onProfileLoaded(profileStatus);
        }
    }


    // ------------- Functions Related to OpenVPN-------------
    private void startVPNConnection(VpnProfile vp) {
        Intent intent = new Intent(activity, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        activity.startActivity(intent);
    }

    private void stopVPNConnection() {

        ProfileManager.setConntectedVpnProfileDisconnected(context);
        if (mService != null) {
            try {
                mService.stopVPN(false);
                onStop();
            } catch (RemoteException e) {
                VpnStatus.logException(e);
            }
        }
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("CONNECTED")) {
                    Log.e("status", "connected");
                    App.isStart = true;
                    setVPNStatus(true);
                }

                if (state.equals("AUTH_FAILED")) {
                    Toast.makeText(activity, "Wrong Username or Password!", Toast.LENGTH_SHORT).show();
                    setVPNStatus(false);
                }


            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {

    }

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {

    }


}



