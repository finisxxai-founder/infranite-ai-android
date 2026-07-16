package com.infranite.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import com.infranite.app.network.ConnectivityObserver

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var connectivityObserver: ConnectivityObserver
    private val PERMISSION_REQUEST_CODE = 100
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "InfraniteAI/1.0"
            }
            
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            
            downloadListener = DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                downloadFile(url, contentDisposition, mimetype)
            }
            
            addJavascriptInterface(JSBridge(this@MainActivity), "androidBridge")
        }
        
        setContentView(webView)
        
        // Load the web application
        webView.loadUrl("https://infranite.ai")
        
        // Setup connectivity observer
        connectivityObserver = ConnectivityObserver(this)
        connectivityObserver.observeConnectivity()
        
        // Request permissions
        requestPermissions()
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
    }
    
    private fun downloadFile(url: String, contentDisposition: String, mimetype: String) {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Downloading...")
            setDescription(contentDisposition)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, contentDisposition.split("filename=").lastOrNull()?.trim('"') ?: "download")
        }
        
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        connectivityObserver.stopObserving()
    }
}

class JSBridge(private val context: Context) {
    @JavascriptInterface
    fun showMessage(message: String) {
        android.util.Log.d("InfraniteAI", message)
    }
}
