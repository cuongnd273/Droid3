package com.jkenneth.droidovpn.ui.activity;


import android.Manifest;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.jkenneth.droidovpn.OboloiVPN;
import com.jkenneth.droidovpn.OnVPNStatusChangeListener;
import com.jkenneth.droidovpn.R;
import com.jkenneth.droidovpn.model.Server;
import com.jkenneth.droidovpn.util.OvpnUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Shows Server Details
 * <p>
 * Copyright (C) 2015  Jhon Kenneth Carino
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Created by Jhon Kenneth Carino on 10/26/2015.
 */
public class ServerDetailsActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_DETAILS = "server_details";
    private static final int RC_WRITE_EXTERNAL_STORAGE_PERM = 123;

    private CoordinatorLayout rootView;
    private Server server;
    Button btnStartVPN;
    Button btnDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_details);

        if (getIntent() == null) {
            Toast.makeText(this.getApplicationContext(),
                    getString(R.string.invalid_server),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the root view
        rootView = findViewById(R.id.root_view);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up import profile
        Button importProfileButton = findViewById(R.id.btn_import);
        btnStartVPN = findViewById(R.id.btn_start_vpn);
        btnDisconnect = findViewById(R.id.btn_disconnect_vpn);
        importProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importToOpenVpn();
            }
        });
        btnStartVPN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVPN();
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oboloiVPN.stopVPN();
            }
        });

        Bundle extras = getIntent().getExtras();
        server = (Server) extras.getSerializable(EXTRA_DETAILS);

        updateUi();
        initOpenVPN();
    }

    OboloiVPN oboloiVPN;

    private void initOpenVPN() {
        oboloiVPN = new OboloiVPN(this, getApplicationContext());
        oboloiVPN.setOnVPNStatusChangeListener(new OnVPNStatusChangeListener() {

            @Override
            public void onProfileLoaded(boolean profileLoaded) {
                Log.e("TAG", "onProfileLoaded: " + profileLoaded);
                if (profileLoaded) {
                    oboloiVPN.init();
                } else {
//                    Toast.makeText(ServerDetailsActivity.this, "ProfileLoaded:"+ false, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onVPNStatusChanged(boolean vpnActivated) {
                Log.e("TAG", "onVPNStatusChanged: " + vpnActivated);
                if (vpnActivated) {
                    btnStartVPN.setText("disconnect");
                } else {
                    btnStartVPN.setText("connect");
                }
            }

        });
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE_PERM)
    private void startVPN() {
        File file = OvpnUtils.saveConfigData(ServerDetailsActivity.this, server);
//        if (file!=null){
//            oboloiVPN.launchVPN(file);
//        }else {
//            Toast.makeText(this, "Create file error", Toast.LENGTH_SHORT).show();
//        }
        try {
            if (file.exists()) {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                OpenVpnApi.startVpn(this, text.toString(), "vpn", "vpn");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            OvpnUtils.shareOvpnFile(this, server);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUi() {
        setText(R.id.tv_country_name, server.countryLong);
        setText(R.id.tv_host_name, server.hostName);
        setText(R.id.tv_ip_address, server.ipAddress);
        setText(R.id.tv_port, String.valueOf(server.port));
        setText(R.id.tv_protocol, server.protocol.toUpperCase());
        setText(R.id.tv_speed, OvpnUtils.humanReadableCount(server.speed, true));
        setText(R.id.tv_ping, String.format(getString(R.string.format_ping), server.ping));
        setText(R.id.tv_vpn_sessions, String.valueOf(server.vpnSessions));
        setText(R.id.tv_uptime, String.valueOf(server.uptime));
        setText(R.id.tv_total_users, String.valueOf(server.totalUsers));
        setText(R.id.tv_total_traffic, OvpnUtils.humanReadableCount(
                Long.valueOf(server.totalTraffic), false));
        setText(R.id.tv_logging_policy, server.logType);
        setText(R.id.tv_operator_name, server.operator);
        setText(R.id.tv_operator_message, server.message);
    }

    private void setText(int textView, String text) {
        text = !TextUtils.isEmpty(text) ? text : "-";
        ((TextView) findViewById(textView)).setText(text);
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE_PERM)
    private void importToOpenVpn() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            OvpnUtils.importToOpenVpn(ServerDetailsActivity.this, server);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_write_external),
                    RC_WRITE_EXTERNAL_STORAGE_PERM, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            Snackbar.make(rootView, R.string.permission_denied_message,
                    Snackbar.LENGTH_SHORT).show();
        }
    }
}
