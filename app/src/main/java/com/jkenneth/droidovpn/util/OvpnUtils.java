package com.jkenneth.droidovpn.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import android.util.Log;

import com.jkenneth.droidovpn.R;
import com.jkenneth.droidovpn.model.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Jhon Kenneth Carino on 10/18/15.
 */
public class OvpnUtils {

    private static final String TAG = "OvpnUtils";
    private static final String FILE_EXTENSION = ".ovpn";
    private static final String OPENVPN_PKG_NAME = "net.openvpn.openvpn";
    private static final String OPENVPN_MIME_TYPE = "application/x-openvpn-profile";

    /**
     * Imports OVPN configuration into OpenVPN Connect app (net.openvpn.openvpn), if available.
     * Otherwise, this opens Google Play Store to install OpenVPN Connect app.
     *
     * @param activity The context of an activity
     * @param server The {@link Server} that contains OVPN profile you want to import.
     */
    public static void importToOpenVpn(@NonNull final Activity activity, @NonNull Server server) {
        File file = getFile(activity, server);
        if (!file.exists()) {
            saveConfigData(activity, server);
        }

        Uri uri = FileProvider.getUriForFile(activity,
                activity.getApplicationContext().getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(uri, OPENVPN_MIME_TYPE);

        List<ResolveInfo> resolvedIntentActivities = activity.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;

            activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(R.string.title_import_dialog)
                    .setMessage(R.string.message_import_dialog)
                    .setCancelable(false)
                    .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PlayStoreUtils.openApp(activity, OPENVPN_PKG_NAME);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    public static void startVPN(){

    }
    public static String humanReadableCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.2f %s" + (si ? "bps" : "B"),
                bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Writes and saves OVPN profile to a file
     *
     * @param context The context of an application
     * @param server The {@link Server} that contains OVPN profile
     */
    public static File saveConfigData(@NonNull Context context, @NonNull Server server) {
        File file;
        FileOutputStream outputStream;
//        Log.e("TAG", "saveConfigData: "+server.ovpnConfigData );
        try {
            file = getFile(context, server);
            if(!file.exists()){
                file.createNewFile();
            }
            if (!file.exists())
                return file;
            outputStream = new FileOutputStream(file);
            String sv = server.ovpnConfigData+"\n" +
                    "# Please note that if the server certificate is not a self-signed, you have to\n" +
                    "# specify the signer's root certificate (CA) here.\n" +
                    "\n" +
                    "<ca>\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIF3jCCA8agAwIBAgIQAf1tMPyjylGoG7xkDjUDLTANBgkqhkiG9w0BAQwFADCB\n" +
                    "iDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCk5ldyBKZXJzZXkxFDASBgNVBAcTC0pl\n" +
                    "cnNleSBDaXR5MR4wHAYDVQQKExVUaGUgVVNFUlRSVVNUIE5ldHdvcmsxLjAsBgNV\n" +
                    "BAMTJVVTRVJUcnVzdCBSU0EgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkwHhcNMTAw\n" +
                    "MjAxMDAwMDAwWhcNMzgwMTE4MjM1OTU5WjCBiDELMAkGA1UEBhMCVVMxEzARBgNV\n" +
                    "BAgTCk5ldyBKZXJzZXkxFDASBgNVBAcTC0plcnNleSBDaXR5MR4wHAYDVQQKExVU\n" +
                    "aGUgVVNFUlRSVVNUIE5ldHdvcmsxLjAsBgNVBAMTJVVTRVJUcnVzdCBSU0EgQ2Vy\n" +
                    "dGlmaWNhdGlvbiBBdXRob3JpdHkwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIK\n" +
                    "AoICAQCAEmUXNg7D2wiz0KxXDXbtzSfTTK1Qg2HiqiBNCS1kCdzOiZ/MPans9s/B\n" +
                    "3PHTsdZ7NygRK0faOca8Ohm0X6a9fZ2jY0K2dvKpOyuR+OJv0OwWIJAJPuLodMkY\n" +
                    "tJHUYmTbf6MG8YgYapAiPLz+E/CHFHv25B+O1ORRxhFnRghRy4YUVD+8M/5+bJz/\n" +
                    "Fp0YvVGONaanZshyZ9shZrHUm3gDwFA66Mzw3LyeTP6vBZY1H1dat//O+T23LLb2\n" +
                    "VN3I5xI6Ta5MirdcmrS3ID3KfyI0rn47aGYBROcBTkZTmzNg95S+UzeQc0PzMsNT\n" +
                    "79uq/nROacdrjGCT3sTHDN/hMq7MkztReJVni+49Vv4M0GkPGw/zJSZrM233bkf6\n" +
                    "c0Plfg6lZrEpfDKEY1WJxA3Bk1QwGROs0303p+tdOmw1XNtB1xLaqUkL39iAigmT\n" +
                    "Yo61Zs8liM2EuLE/pDkP2QKe6xJMlXzzawWpXhaDzLhn4ugTncxbgtNMs+1b/97l\n" +
                    "c6wjOy0AvzVVdAlJ2ElYGn+SNuZRkg7zJn0cTRe8yexDJtC/QV9AqURE9JnnV4ee\n" +
                    "UB9XVKg+/XRjL7FQZQnmWEIuQxpMtPAlR1n6BB6T1CZGSlCBst6+eLf8ZxXhyVeE\n" +
                    "Hg9j1uliutZfVS7qXMYoCAQlObgOK6nyTJccBz8NUvXt7y+CDwIDAQABo0IwQDAd\n" +
                    "BgNVHQ4EFgQUU3m/WqorSs9UgOHYm8Cd8rIDZsswDgYDVR0PAQH/BAQDAgEGMA8G\n" +
                    "A1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEMBQADggIBAFzUfA3P9wF9QZllDHPF\n" +
                    "Up/L+M+ZBn8b2kMVn54CVVeWFPFSPCeHlCjtHzoBN6J2/FNQwISbxmtOuowhT6KO\n" +
                    "VWKR82kV2LyI48SqC/3vqOlLVSoGIG1VeCkZ7l8wXEskEVX/JJpuXior7gtNn3/3\n" +
                    "ATiUFJVDBwn7YKnuHKsSjKCaXqeYalltiz8I+8jRRa8YFWSQEg9zKC7F4iRO/Fjs\n" +
                    "8PRF/iKz6y+O0tlFYQXBl2+odnKPi4w2r78NBc5xjeambx9spnFixdjQg3IM8WcR\n" +
                    "iQycE0xyNN+81XHfqnHd4blsjDwSXWXavVcStkNr/+XeTWYRUc+ZruwXtuhxkYze\n" +
                    "Sf7dNXGiFSeUHM9h4ya7b6NnJSFd5t0dCy5oGzuCr+yDZ4XUmFF0sbmZgIn/f3gZ\n" +
                    "XHlKYC6SQK5MNyosycdiyA5d9zZbyuAlJQG03RoHnHcAP9Dc1ew91Pq7P8yF1m9/\n" +
                    "qS3fuQL39ZeatTXaw2ewh0qpKJ4jjv9cJ2vhsE/zB+4ALtRZh8tSQZXq9EfX7mRB\n" +
                    "VXyNWQKV3WKdwrnuWih0hKWbt5DHDAff9Yk2dDLWKMGwsAvgnEzDHNb842m1R0aB\n" +
                    "L6KCq9NjRHDEjf8tM7qtj3u1cIiuPhnPQCjY/MiQu12ZIvVS5ljFH4gxQ+6IHdfG\n" +
                    "jjxDah2nGN59PRbxYvnKkKj9\n" +
                    "-----END CERTIFICATE-----\n" +
                    "\n" +
                    "</ca>\n" +
                    "\n" +
                    "\n" +
                    "###############################################################################\n" +
                    "# The client certificate file (dummy).\n" +
                    "# \n" +
                    "# In some implementations of OpenVPN Client software\n" +
                    "# (for example: OpenVPN Client for iOS),\n" +
                    "# a pair of client certificate and private key must be included on the\n" +
                    "# configuration file due to the limitation of the client.\n" +
                    "# So this sample configuration file has a dummy pair of client certificate\n" +
                    "# and private key as follows.\n" +
                    "\n" +
                    "<cert>\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIICxjCCAa4CAQAwDQYJKoZIhvcNAQEFBQAwKTEaMBgGA1UEAxMRVlBOR2F0ZUNs\n" +
                    "aWVudENlcnQxCzAJBgNVBAYTAkpQMB4XDTEzMDIxMTAzNDk0OVoXDTM3MDExOTAz\n" +
                    "MTQwN1owKTEaMBgGA1UEAxMRVlBOR2F0ZUNsaWVudENlcnQxCzAJBgNVBAYTAkpQ\n" +
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5h2lgQQYUjwoKYJbzVZA\n" +
                    "5VcIGd5otPc/qZRMt0KItCFA0s9RwReNVa9fDRFLRBhcITOlv3FBcW3E8h1Us7RD\n" +
                    "4W8GmJe8zapJnLsD39OSMRCzZJnczW4OCH1PZRZWKqDtjlNca9AF8a65jTmlDxCQ\n" +
                    "CjntLIWk5OLLVkFt9/tScc1GDtci55ofhaNAYMPiH7V8+1g66pGHXAoWK6AQVH67\n" +
                    "XCKJnGB5nlQ+HsMYPV/O49Ld91ZN/2tHkcaLLyNtywxVPRSsRh480jju0fcCsv6h\n" +
                    "p/0yXnTB//mWutBGpdUlIbwiITbAmrsbYnjigRvnPqX1RNJUbi9Fp6C2c/HIFJGD\n" +
                    "ywIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQChO5hgcw/4oWfoEFLu9kBa1B//kxH8\n" +
                    "hQkChVNn8BRC7Y0URQitPl3DKEed9URBDdg2KOAz77bb6ENPiliD+a38UJHIRMqe\n" +
                    "UBHhllOHIzvDhHFbaovALBQceeBzdkQxsKQESKmQmR832950UCovoyRB61UyAV7h\n" +
                    "+mZhYPGRKXKSJI6s0Egg/Cri+Cwk4bjJfrb5hVse11yh4D9MHhwSfCOH+0z4hPUT\n" +
                    "Fku7dGavURO5SVxMn/sL6En5D+oSeXkadHpDs+Airym2YHh15h0+jPSOoR6yiVp/\n" +
                    "6zZeZkrN43kuS73KpKDFjfFPh8t4r1gOIjttkNcQqBccusnplQ7HJpsk\n" +
                    "-----END CERTIFICATE-----\n" +
                    "\n" +
                    "</cert>\n" +
                    "\n" +
                    "<key>\n" +
                    "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIEpAIBAAKCAQEA5h2lgQQYUjwoKYJbzVZA5VcIGd5otPc/qZRMt0KItCFA0s9R\n" +
                    "wReNVa9fDRFLRBhcITOlv3FBcW3E8h1Us7RD4W8GmJe8zapJnLsD39OSMRCzZJnc\n" +
                    "zW4OCH1PZRZWKqDtjlNca9AF8a65jTmlDxCQCjntLIWk5OLLVkFt9/tScc1GDtci\n" +
                    "55ofhaNAYMPiH7V8+1g66pGHXAoWK6AQVH67XCKJnGB5nlQ+HsMYPV/O49Ld91ZN\n" +
                    "/2tHkcaLLyNtywxVPRSsRh480jju0fcCsv6hp/0yXnTB//mWutBGpdUlIbwiITbA\n" +
                    "mrsbYnjigRvnPqX1RNJUbi9Fp6C2c/HIFJGDywIDAQABAoIBAERV7X5AvxA8uRiK\n" +
                    "k8SIpsD0dX1pJOMIwakUVyvc4EfN0DhKRNb4rYoSiEGTLyzLpyBc/A28Dlkm5eOY\n" +
                    "fjzXfYkGtYi/Ftxkg3O9vcrMQ4+6i+uGHaIL2rL+s4MrfO8v1xv6+Wky33EEGCou\n" +
                    "QiwVGRFQXnRoQ62NBCFbUNLhmXwdj1akZzLU4p5R4zA3QhdxwEIatVLt0+7owLQ3\n" +
                    "lP8sfXhppPOXjTqMD4QkYwzPAa8/zF7acn4kryrUP7Q6PAfd0zEVqNy9ZCZ9ffho\n" +
                    "zXedFj486IFoc5gnTp2N6jsnVj4LCGIhlVHlYGozKKFqJcQVGsHCqq1oz2zjW6LS\n" +
                    "oRYIHgECgYEA8zZrkCwNYSXJuODJ3m/hOLVxcxgJuwXoiErWd0E42vPanjjVMhnt\n" +
                    "KY5l8qGMJ6FhK9LYx2qCrf/E0XtUAZ2wVq3ORTyGnsMWre9tLYs55X+ZN10Tc75z\n" +
                    "4hacbU0hqKN1HiDmsMRY3/2NaZHoy7MKnwJJBaG48l9CCTlVwMHocIECgYEA8jby\n" +
                    "dGjxTH+6XHWNizb5SRbZxAnyEeJeRwTMh0gGzwGPpH/sZYGzyu0SySXWCnZh3Rgq\n" +
                    "5uLlNxtrXrljZlyi2nQdQgsq2YrWUs0+zgU+22uQsZpSAftmhVrtvet6MjVjbByY\n" +
                    "DADciEVUdJYIXk+qnFUJyeroLIkTj7WYKZ6RjksCgYBoCFIwRDeg42oK89RFmnOr\n" +
                    "LymNAq4+2oMhsWlVb4ejWIWeAk9nc+GXUfrXszRhS01mUnU5r5ygUvRcarV/T3U7\n" +
                    "TnMZ+I7Y4DgWRIDd51znhxIBtYV5j/C/t85HjqOkH+8b6RTkbchaX3mau7fpUfds\n" +
                    "Fq0nhIq42fhEO8srfYYwgQKBgQCyhi1N/8taRwpk+3/IDEzQwjbfdzUkWWSDk9Xs\n" +
                    "H/pkuRHWfTMP3flWqEYgW/LW40peW2HDq5imdV8+AgZxe/XMbaji9Lgwf1RY005n\n" +
                    "KxaZQz7yqHupWlLGF68DPHxkZVVSagDnV/sztWX6SFsCqFVnxIXifXGC4cW5Nm9g\n" +
                    "va8q4QKBgQCEhLVeUfdwKvkZ94g/GFz731Z2hrdVhgMZaU/u6t0V95+YezPNCQZB\n" +
                    "wmE9Mmlbq1emDeROivjCfoGhR3kZXW1pTKlLh6ZMUQUOpptdXva8XxfoqQwa3enA\n" +
                    "M7muBbF0XN7VO80iJPv+PmIZdEIAkpwKfi201YB+BafCIuGxIF50Vg==\n" +
                    "-----END RSA PRIVATE KEY-----\n" +
                    "\n" +
                    "</key>\n" +
                    "\n";
            outputStream.write(sv.getBytes("UTF-8"));
            outputStream.close();
            Log.e(TAG, "saveConfigData: "+file.getAbsolutePath() );
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * Creates an empty file for OVPN profile
     *
     * @param context The context of an application
     * @param server The {@link Server} that contains OVPN profile
     */
    private static File getFile(@NonNull Context context, @NonNull Server server) {
        File filePath;
//        if (!Environment.isExternalStorageRemovable() || isExternalStorageWritable()) {
//            filePath = context.getExternalCacheDir();
//        } else {
//            filePath = context.getCacheDir();
//        }
        filePath  = Environment.getExternalStorageDirectory();
        return new File(filePath, server.countryShort + "_" + server.hostName + "_" +
                server.protocol.toUpperCase() + FILE_EXTENSION);
    }

    /**
     * @return Whether the external storage is available for read and write.
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static int getDrawableResource(@NonNull Context context, @NonNull String resource) {
        return context.getResources()
                .getIdentifier(resource, "drawable", context.getPackageName());
    }

    /**
     * Shows an intent chooser to share OVPN profile.
     *
     * @param activity The context of an activity
     * @param server The {@link Server} that contains OVPN profile
     */
    public static void shareOvpnFile(@NonNull Activity activity, @NonNull Server server) {
        File file = getFile(activity, server);
        if (!file.exists()) {
            saveConfigData(activity, server);
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getFile(activity, server)));
        activity.startActivity(Intent.createChooser(intent, "Share Profile using"));
    }
}
