package com.example.webviewdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.webviewdemo.widget.CostomeWebView;

public class WebViewActivity extends AppCompatActivity {

    private CostomeWebView costomeWebView;
    private Button btnAndroidToJs;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initView();
        checkPermission();
        setFullScreen();
        url = getIntent().getStringExtra("url");
        costomeWebView.init(this, url, true);
        //js调用Android  android.send();
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
