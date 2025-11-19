package com.mobi.nubitalcatel.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobi.nubitalcatel.R

class WebviewActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        window.statusBarColor = Color.WHITE  // Set light background
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val webView = findViewById<WebView>(R.id.webView)
        val ivBackToolbar = findViewById<ImageView>(R.id.ivBackToolbar)
        val txtToolbarTitle = findViewById<TextView>(R.id.txtToolbarTitle)

        if (intent.getStringExtra("title").toString().isNullOrEmpty() || intent.getStringExtra("title").toString() == "null"){
            txtToolbarTitle.setText("Nubit")
        }else{
            txtToolbarTitle.setText(intent.getStringExtra("title").toString())
        }
        ivBackToolbar.setOnClickListener { onBackPressed() }
        // Enable JavaScript (if your site needs it)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Make links and redirects open inside the WebView instead of a browser
        webView.webViewClient = WebViewClient()

        // Optional: Show loading progress or title updates
        webView.webChromeClient = WebChromeClient()

        // Load a URL
        webView.loadUrl(intent.getStringExtra("url").toString())
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}