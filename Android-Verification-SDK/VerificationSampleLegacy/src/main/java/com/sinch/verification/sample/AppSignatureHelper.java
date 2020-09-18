package com.sinch.verification.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.NameNotFoundException;

@SuppressLint("all")

/*
  This is a helper class to generate your message hash to be included in your SMS message.

  Without the correct hash, your app won't recieve the message callback. This only needs to be
  generated once per app and stored. Then you can remove this helper class from your code.
*/

public class AppSignatureHelper extends ContextWrapper {
    public static final String TAG = AppSignatureHelper.class.getSimpleName();
    private static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;

    public AppSignatureHelper(Context context) {
        super(context);
        getAppSignatures();
    }

    /**
     * Get all the app signatures for the current package
     * Note: SupressLint and SuppressWarnings annotations are needed as getting signatures programmatically can lead tu vulnerability issues.
     * [SEE] (https://blog.checkpoint.com/2014/07/29/android-fake-id/) for more details.
     * In you production application calculate your key as described
     * in the [docs](https://developers.sinch.com/docs/verification-android-the-verification-process#automatic-code-extraction-from-sms).
     */
    @SuppressLint("PackageManagerGetSignatures")
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    public ArrayList<String> getAppSignatures() {
        ArrayList<String> appCodes = new ArrayList<>();

        try {
            // Get all package signatures for the current package
            String packageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            @SuppressLint("PackageManagerGetSignatures") Signature[] signatures = packageManager.getPackageInfo(packageName,
                    GET_SIGNATURES).signatures;

            // For each signature create a compatible hash
            for (Signature signature : signatures) {
                String hash = hash(packageName, signature.toCharsString());
                if (hash != null) {
                    appCodes.add(String.format("%s", hash));
                }

                Log.v(TAG, "Hash " + hash);

            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to find package to obtain hash.", e);
        }
        return appCodes;
    }

    @SuppressLint("ObsoleteSdkInt")
    private static String hash(String packageName, String signature) {
        String appInfo = packageName + " " + signature;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
            }
            byte[] hashSignature = messageDigest.digest();

            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
            // encode into Base64
            String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

            Log.d(TAG, String.format("pkg: %s -- hash: %s", packageName, base64Hash));
            return base64Hash;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "hash:NoSuchAlgorithm", e);
        }
        return null;
    }
}