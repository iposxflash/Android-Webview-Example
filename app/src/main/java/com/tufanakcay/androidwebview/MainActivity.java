package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

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

        webView.getSettings().setJavaScriptEnabled(true);
        
        // 🎯 PERBAIKAN KRITIS: Muat URL dari strings.xml
        String dynamicUrl = getString(R.string.web_url); 
        
        // Gunakan URL yang dimuat dari strings.xml, yang diubah oleh GitHub Actions
        webView.loadUrl(dynamicUrl);
    }

    private void viewHtml() {

        String htmlCode = "<html><head><title>Webview HTML Example</title></head><body><h1>HTML EXAMPLE</h1></body></html>";
        webView.loadData(htmlCode, "text/html", "UTF-8");
    }

}
