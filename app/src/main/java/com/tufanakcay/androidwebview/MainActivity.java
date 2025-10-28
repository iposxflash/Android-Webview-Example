package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient; // Penting untuk mencegah redirect ke Chrome

public class MainActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        viewUrl();
    }

    private void init() {
        webView = findViewById(R.id.webView);
    }

    private void viewUrl() {

        // 🎯 KRITIS: Muat URL dari strings.xml yang telah diubah YML
        String dynamicUrl = getString(R.string.web_url); 

        webView.getSettings().setJavaScriptEnabled(true);
        
        // Memastikan link tetap di dalam WebView
        webView.setWebViewClient(new WebViewClient()); 

        webView.loadUrl(dynamicUrl);
    }
}
