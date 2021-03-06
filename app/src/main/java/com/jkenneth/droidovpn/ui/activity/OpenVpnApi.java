package com.jkenneth.droidovpn.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.apero.openvpn.VpnProfile;
import com.apero.openvpn.core.ConfigParser;
import com.apero.openvpn.core.ProfileManager;
import com.apero.openvpn.core.VPNLaunchHelper;
import com.jkenneth.droidovpn.R;

import java.io.IOException;
import java.io.StringReader;

public class OpenVpnApi {
    /**
     * @param inlineConfig 一般都保存在文件中，通过读取文件获取配置信息
     * @param userName     某些ovpn的连接方式需要用户名和密码，可以为空
     * @param pw           某些ovpn的连接方式需要用户名和密码，可以为空
     * @throws RemoteException
     */
    public static void startVpn(Context context, String inlineConfig, String userName, String pw) throws RemoteException {
        if (TextUtils.isEmpty(inlineConfig)) throw new RemoteException("config is empty");
        Intent intent = VpnService.prepare(context);
        if (intent != null) {
            intent = new Intent(context, VpnAuthActivity.class);
            intent.putExtra(VpnAuthActivity.KEY_CONFIG, inlineConfig);
            if (!TextUtils.isEmpty(userName)) {
                intent.putExtra(VpnAuthActivity.KEY_USERNAME, userName);
            }
            if (!TextUtils.isEmpty(pw)) {
                intent.putExtra(VpnAuthActivity.KEY_PASSWORD, pw);
            }
            context.startActivity(intent);
        } else {
            startVpnInternal(context, inlineConfig, userName, pw);
        }
    }

    static void startVpnInternal(Context context, String inlineConfig, String userName, String pw) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(inlineConfig));
            VpnProfile vp = cp.convertProfile();
            vp.mName = Build.MODEL;
            if (vp.checkProfile(context) != R.string.no_error_found){
                throw new RemoteException(context.getString(vp.checkProfile(context)));
            }
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = userName;
            vp.mPassword = pw;
            ProfileManager.setTemporaryProfile(context,vp);
            VPNLaunchHelper.startOpenVpn(vp, context);
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }
    }
}