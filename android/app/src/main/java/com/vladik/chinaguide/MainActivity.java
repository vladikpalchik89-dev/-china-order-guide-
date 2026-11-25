package com.vladik.chinaguide;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web);
        web.setBackgroundColor(Color.rgb(245, 247, 251));

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception ignored) {
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(
                    "(function(){ if(!window.AndroidBridge) return;" +
                    "document.addEventListener('click', function(e){" +
                    "  var b = e.target && e.target.closest ? e.target.closest('[data-copy]') : null;" +
                    "  if(b){ var el = document.getElementById(b.getAttribute('data-copy'));" +
                    "         if(el) AndroidBridge.copyText(String(el.textContent)); }" +
                    "  var a = e.target && e.target.closest ? e.target.closest('a[download]') : null;" +
                    "  if(a && String(a.href).indexOf('blob:') === 0){" +
                    "    var nm = a.download || 'otchet.txt';" +
                    "    fetch(a.href).then(function(r){ return r.blob(); }).then(function(bl){" +
                    "      var fr = new FileReader();" +
                    "      fr.onload = function(){ AndroidBridge.saveFile(nm, String(fr.result).split(',')[1]); };" +
                    "      fr.readAsDataURL(bl);" +
                    "    });" +
                    "  }" +
                    "}, true);" +
                    "})();", null);
            }
        });

        web.loadUrl("file:///android_asset/www/index.html");
    }

    public class Bridge {
        @JavascriptInterface
        public void copyText(String text) {
            try {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Китай без обмана", text));
            } catch (Exception ignored) {
            }
        }

        @JavascriptInterface
        public void saveFile(String name, String base64) {
            try {
                byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) dir = getFilesDir();
                if (dir != null) {
                    File safe = new File(dir, name.replaceAll("[\\\\/:*?\"<>|]", "_"));
                    FileOutputStream out = new FileOutputStream(safe);
                    out.write(bytes);
                    out.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web != null && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
