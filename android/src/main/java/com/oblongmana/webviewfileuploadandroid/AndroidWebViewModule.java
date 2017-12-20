package com.oblongmana.webviewfileuploadandroid;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ActivityEventListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;
import android.webkit.ValueCallback;

import com.facebook.react.common.annotations.VisibleForTesting;

public class AndroidWebViewModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private Uri mImageUri;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    public static final String TAG = "AndroidWebViewModule";

    @VisibleForTesting
    public static final String REACT_CLASS = "AndroidWebViewModule";

    public AndroidWebViewModule(ReactApplicationContext context) {
        super(context);
        context.addActivityEventListener(this);
    }

    private AndroidWebViewPackage aPackage;

    public void setPackage(AndroidWebViewPackage aPackage) {
        this.aPackage = aPackage;
    }

    public AndroidWebViewPackage getPackage() {
        return this.aPackage;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @SuppressWarnings("unused")
    public Activity getActivity() {
        return getCurrentActivity();
    }

    public void setImageUri(Uri imageUri) {
        mImageUri = imageUri;
    }

    public void setUploadMessage(ValueCallback<Uri> uploadMessage) {
        mUploadMessage = uploadMessage;
    }

    public void setmUploadCallbackAboveL(ValueCallback<Uri[]> mUploadCallbackAboveL) {
        this.mUploadCallbackAboveL = mUploadCallbackAboveL;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == 1) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL){
                return;
            }
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResultAboveL");
        if (requestCode != 1 || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "RESULT_OK");
            if (data != null) {
                ClipData clipData = data.getClipData();
                String dataString = data.getDataString();
                if (clipData != null) {
                    Log.d(TAG, "clipData != null");
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                        Log.d(TAG, item.getUri().toString());
                    }
                } else if (dataString != null) {
                    Log.d(TAG, "dataString != null");
                    Log.d(TAG, dataString);
                    results = new Uri[] { Uri.parse(dataString) };
                } else if (mImageUri != null) {
                    Log.d(TAG, "mImageUri != null");
                    Log.d(TAG, mImageUri.toString());
                    results = new Uri[] { mImageUri };
                }
            } else {
                Log.d(TAG, "data == null");
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
    }

    public void onNewIntent(Intent intent) {
    }
}
