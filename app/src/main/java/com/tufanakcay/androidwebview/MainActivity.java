package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.DownloadListener; // Tambahan
import android.content.Intent; // Tambahan
import android.net.Uri; // Tambahan
import android.os.Build;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.widget.Toast; // Tambahan

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
        String dynamicUrl = getString(R.string.web_url); 

        WebSettings webSettings = webView.getSettings();

        // 1. Aktifkan JavaScript
        webSettings.setJavaScriptEnabled(true);
        
        // 2. Aktifkan DOM Storage
        webSettings.setDomStorageEnabled(true); 
        webSettings.setDatabaseEnabled(true);

        // 3. Pengaturan Akses File
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // 4. Penanganan Keamanan HTTPS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 5. User Agent
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");

        // FITUR CETAK
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void performPrint() {
                runOnUiThread(() -> createWebPrintJob(webView));
            }
        }, "AndroidPrint");

        // --- TAMBAHAN UNTUK FITUR DOWNLOAD ---
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // Mengalihkan download ke Browser bawaan HP (Chrome/Samsung Browser) agar lebih aman dan stabil
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                Toast.makeText(MainActivity.this, "Mengunduh file...", Toast.LENGTH_SHORT).show();
            }
        });
        // -------------------------------------

        // 6. WebChromeClient
        webView.setWebChromeClient(new WebChromeClient());

        // 7. WebViewClient
        webView.setWebViewClient(new CustomWebViewClient()); 

        webView.loadUrl(dynamicUrl);
    }

    private void createWebPrintJob(WebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
            String jobName = getString(R.string.app_name) + " Document";
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
            if (printManager != null) {
                printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false; 
        }

        @Override
        public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
             view.loadUrl("javascript:window.print = function() { AndroidPrint.performPrint(); }");
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                String failingUrl = request.getUrl().toString();
                String htmlData = "<html><body style='display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif; margin:0; background-color:#F5F5F5;'>"
                                + "<div style='text-align:center; padding:20px;'>"
                                + "<h2 style='color:#00529C;'>Koneksi Terputus</h2>"
                                + "<a href='" + failingUrl + "' style='display:inline-block; text-decoration:none; padding:15px 30px; background:#F05A22; color:white; border-radius:8px; font-weight:bold;'>COBA LAGI</a>"
                                + "</div></body></html>";
                view.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
            }
        }
    }
}
