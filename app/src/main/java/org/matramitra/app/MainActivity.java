package org.matramitra.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import java.util.Locale;

/**
 * Shows the Matra Mitra web app from the APK's assets folder, fully offline.
 * Android's WebView has no Web Speech API, so speech goes through a small
 * JavaScript bridge ("AndroidTTS") to the phone's own text-to-speech engine.
 */
public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final String START_URL = "https://appassets.androidplatform.net/assets/index.html";

    private WebView web;
    private TextToSpeech tts;
    private volatile boolean ttsReady = false;
    private volatile boolean hindiAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // keeps game progress and settings
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);                    // ignore the phone's font-size setting so layouts stay intact

        // Serves files in app/src/main/assets over a local https address, so fonts and storage behave as on the web.
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
        });

        web.addJavascriptInterface(new TtsBridge(), "AndroidTTS");

        tts = new TextToSpeech(this, this);
        web.loadUrl(START_URL);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            ttsReady = false;
            return;
        }
        int result = tts.setLanguage(new Locale("hi", "IN"));
        hindiAvailable = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String id) { sendEvent(id, "start"); }

            @Override
            public void onDone(String id) { sendEvent(id, "end"); }

            @Override
            public void onStop(String id, boolean interrupted) { sendEvent(id, "end"); }

            @Override
            @SuppressWarnings("deprecation")
            public void onError(String id) { sendEvent(id, "error"); }

            @Override
            public void onError(String id, int errorCode) { sendEvent(id, "error"); }
        });

        ttsReady = true;
        runJs("window.__ttsReady && window.__ttsReady();");
    }

    private void sendEvent(String id, String event) {
        if (id == null) return;
        String safeId = id.replaceAll("[^A-Za-z0-9_]", "");
        runJs("window.__ttsEvent && window.__ttsEvent('" + safeId + "','" + event + "');");
    }

    private void runJs(final String code) {
        if (web == null) return;
        web.post(() -> {
            if (web != null) web.evaluateJavascript(code, null);
        });
    }

    /** Methods the page can call as window.AndroidTTS.speak(...), etc. */
    private class TtsBridge {
        @JavascriptInterface
        public boolean available() { return ttsReady; }

        @JavascriptInterface
        public boolean hasHindi() { return hindiAvailable; }

        @JavascriptInterface
        public void speak(String text, float rate, String id) {
            if (!ttsReady || tts == null) {
                sendEvent(id, "error");
                return;
            }
            tts.setSpeechRate(Math.max(0.4f, Math.min(rate, 1.5f)));
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id);
        }

        @JavascriptInterface
        public void stop() {
            if (tts != null) tts.stop();
        }

        @JavascriptInterface
        public void openTtsSettings() {
            runOnUiThread(() -> {
                try {
                    Intent i = new Intent("com.android.settings.TTS_SETTINGS");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } catch (Exception e) {
                    try {
                        Intent i = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    } catch (Exception ignored) { }
                }
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        // Let the page close a dialog or go back to the first tab before leaving the app.
        web.evaluateJavascript("(window.__onBack && window.__onBack()) ? 'y' : 'n'", value -> {
            if (!"\"y\"".equals(value)) finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The user may have just installed Hindi voice data in the phone settings.
        if (tts != null && ttsReady) {
            int result = tts.setLanguage(new Locale("hi", "IN"));
            hindiAvailable = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            runJs("window.__ttsReady && window.__ttsReady();");
        }
    }

    @Override
    protected void onPause() {
        if (tts != null) tts.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
