package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient; // 🎯 IMPORT BARU

public class MainActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        viewUrl();
        // viewHtml();

    }

    private void init() {
        webView = findViewById(R.id.webView);
    }

    private void viewUrl() {

        // 1. Ambil URL Dinamis dari strings.xml
        String dynamicUrl = getString(R.string.web_url); 

        // 2. Pengaturan WebView
        webView.getSettings().setJavaScriptEnabled(true);
        
        // 🎯 PERBAIKAN KRITIS: Set WebViewClient
        // Ini memastikan bahwa setiap klik link di dalam WebView
        // akan dimuat di WebView itu sendiri, BUKAN di Chrome.
        webView.setWebViewClient(new WebViewClient()); 

        // 3. Muat URL
        webView.loadUrl(dynamicUrl);
    }

    private void viewHtml() {

        String htmlCode = "<html><head><title>Webview HTML Example</title></head><body><h1>HTML EXAMPLE</h1></body></html>";
        webView.loadData(htmlCode, "text/html", "UTF-8");
    }

}
