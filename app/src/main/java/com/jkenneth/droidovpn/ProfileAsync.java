package com.jkenneth.droidovpn;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.apero.openvpn.VpnProfile;
import com.apero.openvpn.core.ConfigParser;
import com.apero.openvpn.core.ProfileManager;

/**
 * ==================================================
 * Created by wang on 2017/11/15. gentlewxy@163.com
 * Description:
 * ==================================================
 */

public class ProfileAsync extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<Context> context;
    private OnProfileLoadListener onProfileLoadListener;
    private String ovpnUrl;
    private File ovpnFile;

    public ProfileAsync(Context context, OnProfileLoadListener onProfileLoadListener, String ovpnUrl) {
        this.context = new WeakReference<>(context);
        this.onProfileLoadListener = onProfileLoadListener;
        this.ovpnUrl = ovpnUrl;
    }

    public ProfileAsync(Context context, OnProfileLoadListener onProfileLoadListener, File File) {
        this.context = new WeakReference<>(context);
        this.onProfileLoadListener = onProfileLoadListener;
        this.ovpnFile = File;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = this.context.get();
        if (context == null || onProfileLoadListener == null) {
            cancel(true);
        } else if (!isNetworkAvailable(context)) {
            cancel(true);
            onProfileLoadListener.onProfileLoadFailed("No Network");
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String path ="";
            InputStream inputStream;
            if (ovpnFile != null) {
//                path = Environment.getExternalStorageDirectory() + "/vpngate_vpn480904115.opengw.net_udp_1981.ovpn";
//                inputStream = new FileInputStream(new File(path));
                inputStream = new FileInputStream(ovpnFile);
            } else {
                URL url = new URL(ovpnUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(10 * 1000);
                httpURLConnection.setReadTimeout(10 * 1000);
                inputStream = httpURLConnection.getInputStream();
            }
            Log.e("TAG", "path: " + path);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream/*, Charset.forName("UTF-8")*/));

            ConfigParser cp = new ConfigParser();
            cp.parseConfig(bufferedReader);
            VpnProfile vp = cp.convertProfile();
            ProfileManager vpl = ProfileManager.getInstance(context.get());
            vp.mName = Build.MODEL;//
            vp.mUsername = "vpn";
            vp.mPassword = "vpn";
            vpl.addProfile(vp);
            vpl.saveProfile(context.get(), vp);
            vpl.saveProfileList(context.get());

            return true;
        } catch (MalformedURLException e) {
            cancel(true);
            onProfileLoadListener.onProfileLoadFailed("MalformedURLException");
            e.printStackTrace();
        } catch (ConfigParser.ConfigParseError configParseError) {
            cancel(true);
            onProfileLoadListener.onProfileLoadFailed("ConfigParseError");
            configParseError.printStackTrace();
        } catch (IOException e) {
            cancel(true);
            onProfileLoadListener.onProfileLoadFailed("IOException");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
        if (aVoid) {
            onProfileLoadListener.onProfileLoadSuccess();
        } else {
            onProfileLoadListener.onProfileLoadFailed("unknown error");
        }
    }

    public interface OnProfileLoadListener {
        void onProfileLoadSuccess();

        void onProfileLoadFailed(String msg);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected();
    }
}
