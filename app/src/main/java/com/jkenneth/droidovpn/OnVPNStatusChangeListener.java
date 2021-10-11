package com.jkenneth.droidovpn;

public interface OnVPNStatusChangeListener
{
    public void onProfileLoaded(boolean profileLoaded);
    public void onVPNStatusChanged(boolean vpnActivated);

}

