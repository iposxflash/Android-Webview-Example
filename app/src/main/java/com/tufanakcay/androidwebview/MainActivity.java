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
import android.webkit.ValueCallback;
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
import androidx.core.content.ContextCompat;
import android.content.Intent; 

public class MainActivity extends AppCompatActivity {

    WebView webView;
    SwipeRefreshLayout swipeRefresh;
    
    // VARIABEL UNTUK UPLOAD GAMBAR
    private ValueCallback<Uri[]> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        viewUrl();
        checkPermissions(); // Gabungan izin download & kamera
    }

    private void init() {
        webView = findViewById(R.id.webView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
        });

        // --- PERBAIKAN SCROLL ---
        // Logika: Jika WebView sedang di-scroll (bukan di posisi paling atas), 
        // maka matikan SwipeRefresh agar tidak mengganggu gerakan jari (scroll chat).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > 0) {
                    swipeRefresh.setEnabled(false);
                } else {
                    swipeRefresh.setEnabled(true);
                }
            });
        }
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.CAMERA};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{Manifest.permission.CAMERA};
        } else {
            permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        }

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
                break;
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
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        // Tambahan agar scroll tidak mentok-mentok (bounce) yang bikin macet
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

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

        // TETAP MENANGANI FILE CHOOSER (KAMERA/GALERI)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                mUploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    mUploadMessage = null;
                    Toast.makeText(MainActivity.this, "Tidak dapat membuka pengelola file", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        webView.setWebViewClient(new CustomWebViewClient()); 
        webView.loadUrl(dynamicUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK && intent != null) {
                String dataString = intent.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            mUploadMessage.onReceiveValue(results);
            mUploadMessage = null;
        }
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
            String url = request.getUrl().toString();

            if (url.startsWith("whatsapp://") || url.contains("api.whatsapp.com") || url.contains("wa.me")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            return false;
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
