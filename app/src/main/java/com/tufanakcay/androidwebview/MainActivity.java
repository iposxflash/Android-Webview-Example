package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

        // 1. Dapatkan Objek WebSettings
        WebSettings webSettings = webView.getSettings();

        // 2. Aktifkan JavaScript
        webSettings.setJavaScriptEnabled(true);
        
        // 3. Aktifkan DOM Storage (Wajib untuk Local Storage)
        webSettings.setDomStorageEnabled(true); 

        // 4. Set Cache Mode ke Default
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // 5. Setting Tampilan
        webSettings.setBuiltInZoomControls(false); 
        webSettings.setDisplayZoomControls(false);
        
        // 6. Gunakan WebViewClient Kustom
        webView.setWebViewClient(new CustomWebViewClient()); 

        webView.loadUrl(dynamicUrl);
    }
    
    // Penanganan tombol back agar tidak langsung keluar aplikasi
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Kelas Kustom untuk menangani loading URL dan Error
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true; 
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
        }

        // Solusi untuk menyembunyikan pesan error bawaan Android
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            // Cek apakah error terjadi pada halaman utama
            if (request.isForMainFrame()) {
                String failingUrl = request.getUrl().toString();
                
                // Tampilan kustom dengan tema warna BRI
                String htmlData = "<html><body style='display:flex; justify-content:center; align-items:center; height:100vh; font-family:sans-serif; margin:0; background-color:#F5F5F5;'>"
                                + "<div style='text-align:center; padding:20px;'>"
                                + "<h2 style='color:#00529C;'>Koneksi Terputus</h2>"
                                + "<p style='color:#666;'>Gagal memuat halaman. Silakan periksa koneksi internet Anda.</p>"
                                + "<br>"
                                + "<a href='" + failingUrl + "' style='display:inline-block; text-decoration:none; padding:15px 30px; background:#F05A22; color:white; border-radius:8px; font-weight:bold; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>COBA LAGI</a>"
                                + "</div></body></html>";
                
                view.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
            }
        }
    }
}
