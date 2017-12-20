package com.oblongmana.webviewfileuploadandroid;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.content.ContentResolver;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.views.webview.ReactWebViewManager;
import com.oblongmana.webviewfileuploadandroid.AndroidWebViewModule;

public class AndroidWebViewManager extends ReactWebViewManager {

    private Activity mActivity = null;
    private AndroidWebViewPackage aPackage;
    public String getName() {
        return "AndroidWebView";
    }

    @Override
    protected WebView createViewInstance(ThemedReactContext reactContext) {
        WebView view = super.createViewInstance(reactContext);
        //Now do our own setWebChromeClient, patching in file chooser support
        final AndroidWebViewModule module = this.aPackage.getModule();
        final ContentResolver contentResolver = reactContext.getContentResolver();
        view.setWebChromeClient(new WebChromeClient(){
            private static final String JPEG_FILE_PREFIX = "IMG_";
            private static final String JPEG_FILE_SUFFIX = ".jpg";

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                module.setUploadMessage(uploadMsg);
                openFileChooserView();
            }

            public boolean onJsConfirm (WebView view, String url, String message, JsResult result) {
                return true;
            }

            public boolean onJsPrompt (WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return true;
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                Log.d(module.TAG, "openFileChooser Android < 3.0");
                module.setUploadMessage(uploadMsg);
                openFileChooserView();
            }

            // For Android > 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d(module.TAG, "openFileChooser Android > 4.1.1");
                module.setUploadMessage(uploadMsg);
                openFileChooserView();
            }

            // For Android > 5.0
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                Log.d(module.TAG, "onShowFileChooser Android > 5.0");
                module.setmUploadCallbackAboveL(filePathCallback);
                openFileChooserView();
                return true;
            }

            // Create a temp image for large picture
            public Uri createImageUri() {
                try {
                    Log.d(module.TAG, "createImageUri");
                    Bitmap image = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "IMG_" + timeStamp;
                    String imagePath = MediaStore.Images.Media.insertImage(contentResolver, image, imageFileName, null);
                    Log.i(module.TAG, imagePath);
                    return Uri.parse(imagePath);
                } catch (Exception e) {
                    Log.d(module.TAG, e.toString());
                    return null;
                }     
            }

            // Let the user select content
            private void openFileChooserView() {
                try {
                    final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                    galleryIntent.setType("image/*,video/*");

                    final Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri imageUri = createImageUri();
                    module.setImageUri(imageUri);
                    imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                    final Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                    final Intent chooserIntent = Intent.createChooser(galleryIntent, "");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {
                        imageIntent,
                        videoIntent
                    });

                    module.getActivity().startActivityForResult(chooserIntent, 1);
                } catch (Exception e) {
                    Log.d(module.TAG, e.toString());
                }
            }
        });
        return view;
    }

    public void setPackage(AndroidWebViewPackage aPackage){
        this.aPackage = aPackage;
    }

    public AndroidWebViewPackage getPackage(){
        return this.aPackage;
    }
}
