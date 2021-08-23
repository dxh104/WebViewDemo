package com.example.webviewdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.webviewdemo.widget.CostomeWebView;

public class WebViewActivity extends AppCompatActivity {

    private CostomeWebView costomeWebView;
    private Button btnAndroidToJs;
    private EditText etUrl;
    private String url = "";
    private String fileDir = "file:///android_asset/index.html";//测试html文件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initView();
        checkPermission();
        initCostomeWebView();
    }

    private void initCostomeWebView() {
        url = getIntent().getStringExtra("url");
        setFullScreen();
        //js调用Android 就在js中用android.send();这个方法
        costomeWebView.setOnReceiveJSMsgListener(new CostomeWebView.JsToAndroid.OnReceiveListener() {
            @Override
            public void onReceive(String msg) {
                Toast.makeText(WebViewActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        btnAndroidToJs.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(final View v) {
                //Android执行js脚本
                costomeWebView.androidToJs("alert('测试');androidToJs('你好，我是Android');", new CostomeWebView.OnAndroidToJsCallBack() {
                    @Override
                    public void onReceiveValue(String value) {
                        Toast.makeText(WebViewActivity.this, value, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //网页下载监听事件
        costomeWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.i("WebViewActivity-------", "onDownloadStart: url=" + url + " mimetype=" + mimetype + " contentLength=" + contentLength);
            }
        });
        //页面变化会回调最新url
        costomeWebView.setOnPageChangeListener(new CostomeWebView.OnPageChangeListener() {
            @Override
            public void onGoPage(String url) {
                Log.i("WebViewActivity-------", "onGoPage: url=" + url);
                etUrl.setText(url);
            }

            @Override
            public void onBackPage(String url) {
                Log.i("WebViewActivity-------", "onBackPage: url=" + url);
                etUrl.setText(url);
            }
        });
        costomeWebView.init(this, url, true);
    }

    private void setFullScreen() {
        getSupportActionBar().hide();//AppCompatActivity去标题栏
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        costomeWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        costomeWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        costomeWebView.onDestroy();
    }

    private void initView() {
        costomeWebView = (CostomeWebView) findViewById(R.id.costomeWebView);
        btnAndroidToJs = (Button) findViewById(R.id.btn_androidToJs);
        etUrl = (EditText) findViewById(R.id.et_url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (costomeWebView.isKeyDownConsumeBack(keyCode))
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//适配6.0权限
            if (ContextCompat.checkSelfPermission(getApplication(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplication(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
}
