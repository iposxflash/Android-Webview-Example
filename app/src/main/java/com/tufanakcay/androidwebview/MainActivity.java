package com.tufanakcay.androidwebview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
// IMPORT TAMBAHAN UNTUK WHATSAPP
import android.content.Intent; 

public class MainActivity extends AppCompatActivity {

    WebView webView;
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        viewUrl();
        checkDownloadPermission();
    }

    private void init() {
        webView = findViewById(R.id.webView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
        });
    }

    private void checkDownloadPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void viewUrl() {
        String dynamicUrl = getString(R.string.web_url); 
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); 
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void performPrint() {
                runOnUiThread(() -> createWebPrintJob(webView));
            }
        }, "AndroidPrint");

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            request.setTitle(fileName);
            request.setDescription("Mengunduh file...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Mengunduh: " + fileName, Toast.LENGTH_LONG).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
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
            // AMBIL URL DARI REQUEST
            String url = request.getUrl().toString();

            // LOGIKA DETEKSI WHATSAPP
            if (url.startsWith("whatsapp://") || url.contains("api.whatsapp.com") || url.contains("wa.me")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true; // Berhenti memuat di WebView, lempar ke WhatsApp
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            
            // LOGIKA UNTUK TELEPON (OPSIONAL TAPI BERGUNA)
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            return false; // Tetap muat URL normal di WebView
        }

        @Override
        public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
             swipeRefresh.setRefreshing(false); 
             view.loadUrl("javascript:window.print = function() { AndroidPrint.performPrint(); }");
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                swipeRefresh.setRefreshing(false);
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
