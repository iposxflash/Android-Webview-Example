package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient; // Tambahan penting
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;

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

        // 1. Aktifkan JavaScript (Wajib)
        webSettings.setJavaScriptEnabled(true);
        
        // 2. Aktifkan Storage (Sangat penting untuk website modern/PWA)
        webSettings.setDomStorageEnabled(true); 
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true); // Untuk performa loading

        // 3. Izinkan akses file (Penting jika ada fitur download/upload di web)
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // 4. Penanganan Keamanan HTTPS (Mixed Content)
        // Agar tombol tidak blokir saat web memanggil script dari luar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 5. User Agent (Opsional: Agar web mengenali sebagai browser mobile standar)
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");

        // 6. WebChromeClient (WAJIB: Agar tombol Alert, Dialog, dan Pop-up berfungsi)
        webView.setWebChromeClient(new WebChromeClient());

        // 7. WebViewClient Kustom
        webView.setWebViewClient(new CustomWebViewClient()); 

        webView.loadUrl(dynamicUrl);
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
        // Gunakan parameter WebResourceRequest untuk kompatibilitas versi baru
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // Biarkan WebView menangani navigasi internal secara otomatis
            return false; 
        }

        // Kompatibilitas untuk versi Android lama
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                String failingUrl = request.getUrl().toString();
                
                String htmlData = "<html><body style='display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif; margin:0; background-color:#F5F5F5;'>"
                                + "<div style='text-align:center; padding:20px;'>"
                                + "<h2 style='color:#00529C;'>Koneksi Terputus</h2>"
                                + "<p style='color:#666;'>Gagal memuat halaman. Silakan periksa koneksi internet Anda.</p>"
                                + "<br>"
                                + "<a href='" + failingUrl + "' style='display:inline-block; text-decoration:none; padding:15px 30px; background:#F05A22; color:white; border-radius:8px; font-weight:bold;'>COBA LAGI</a>"
                                + "</div></body></html>";
                
                view.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
            }
        }
    }
}
