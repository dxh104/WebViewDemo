package com.example.webviewdemo.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.webviewdemo.R;

/**
 * Create by DXH on 2021/08/17
 */
public class CostomeWebView extends ConstraintLayout {
    public String fileDir = "file:///android_asset/index.html";//测试html文件

    private String TAG = getClass().getName() + "--------->";

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private RelativeLayout contentParentView;//播放视频容器
    private WebSettings settings;

    private Drawable mProgressDrawable;//进度条样式
    private int mProgressWidth;//进度条宽度
    private int mProgressHeight;//进度条高度
    private int mWebViewWidth;//WebView宽度
    private int mWebViewHeight;//WebView高度

    private AppCompatActivity mActivity;
    private String mUrl;
    private boolean isActivityFullScreen = false;//Activity是不是全屏

    private JsToAndroid.OnReceiveListener onReceiveListener;

    public CostomeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CostomeWebView);
        mProgressDrawable = array.getDrawable(R.styleable.CostomeWebView_costomeWebView_progressDrawable);
        mProgressWidth = array.getLayoutDimension(R.styleable.CostomeWebView_costomeWebView_progressWidth, -2);
        mProgressHeight = array.getLayoutDimension(R.styleable.CostomeWebView_costomeWebView_progressHeight, -2);
        mWebViewWidth = array.getLayoutDimension(R.styleable.CostomeWebView_costomeWebView_webViewWidth, -2);
        mWebViewHeight = array.getLayoutDimension(R.styleable.CostomeWebView_costomeWebView_webViewHeight, -2);
        array.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!TextUtils.isEmpty(mUrl))
            loadUrl(mUrl);
    }

    private ConstraintLayout.LayoutParams mProgressBarLayoutParams;
    private ConstraintLayout.LayoutParams mWebViewLayoutParams;
    private ConstraintLayout.LayoutParams contentParentViewLayoutParams;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mProgressBarLayoutParams.startToStart = 0;
        mProgressBarLayoutParams.endToEnd = 0;
        mProgressBarLayoutParams.topToTop = 0;
        mProgressBarLayoutParams.width = mProgressWidth == -1 ? getMeasuredWidth() : mProgressWidth;
        mProgressBarLayoutParams.height = mProgressHeight == -1 ? getMeasuredHeight() : mProgressHeight;

        mWebViewLayoutParams.startToStart = 0;
        mWebViewLayoutParams.endToEnd = 0;
        mWebViewLayoutParams.topToTop = 0;
        mWebViewLayoutParams.width = mWebViewWidth == -1 ? getMeasuredWidth() : mWebViewWidth;
        mWebViewLayoutParams.height = mWebViewHeight == -1 ? getMeasuredHeight() : mWebViewHeight;

        contentParentViewLayoutParams.startToStart = 0;
        contentParentViewLayoutParams.endToEnd = 0;
        contentParentViewLayoutParams.topToTop = 0;
        contentParentViewLayoutParams.bottomToBottom = 0;
        contentParentViewLayoutParams.width = getMeasuredWidth();
        contentParentViewLayoutParams.height = getMeasuredHeight();
    }

    /**
     * 初始化
     *
     * @param activity
     * @param url                  初始化页面载入
     * @param isActivityFullScreen 如果WebViewActivity是全屏(没有标题栏和状态栏) 就传true 否则传false
     */
    public void init(AppCompatActivity activity, String url, boolean isActivityFullScreen) {
        mActivity = activity;
        mUrl = url;
        this.isActivityFullScreen = isActivityFullScreen;
        mWebView = new WebView(getContext());
        mProgressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        contentParentView = new RelativeLayout(getContext());
        this.addView(mWebView);
        this.addView(mProgressBar);
        this.addView(contentParentView);
        mProgressBar.setProgressDrawable(mProgressDrawable);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);
        initListener();
        initWebViewSettings();
        mWebViewLayoutParams = (LayoutParams) mWebView.getLayoutParams();
        mProgressBarLayoutParams = (LayoutParams) mProgressBar.getLayoutParams();
        contentParentViewLayoutParams = (LayoutParams) contentParentView.getLayoutParams();

        contentParentView.setBackgroundColor(Color.parseColor("#000000"));//黑色背景
        contentParentView.setVisibility(INVISIBLE);//默认隐藏
    }

    //初始化WebView配置
    private void initWebViewSettings() {
        settings = mWebView.getSettings();
//        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 设置JS是否可以打开WebView新窗口
        settings.setDefaultTextEncodingName("utf-8");// 设置编码格式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);//设置WebView缓存模式 默认断网情况下不缓存
        settings.setPluginState(WebSettings.PluginState.ON);//让WebView支持播放插件
        settings.setSupportZoom(true); // 支持缩放
        settings.setBuiltInZoomControls(true); // 支持手势缩放
        settings.setDisplayZoomControls(false); // 不显示缩放按钮
        settings.setAllowFileAccess(true);//设置在WebView内部是否允许访问文件
        if (Build.VERSION.SDK_INT >= 19) {
            settings.setLoadsImagesAutomatically(true);//支持自动加载图片
        } else {
            settings.setLoadsImagesAutomatically(false);
        }
        settings.setDatabaseEnabled(true);//数据库存储API是否可用，默认值false。
        settings.setSaveFormData(true);//WebView是否保存表单数据，默认值true。
        settings.setDomStorageEnabled(true);//DOM存储API是否可用，默认false。
        settings.setGeolocationEnabled(true);//定位是否可用，默认为true。
        settings.setAppCacheEnabled(true);//应用缓存API是否可用
        settings.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        settings.setLoadWithOverviewMode(true); // 自适应屏幕
        settings.setBlockNetworkImage(false);//解决图片不显示
        settings.setTextZoom(100);//设置默认缩放比例，防止网页跟随系统字体大小变化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//播放网络视频
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//允许WebView同时加载Https和Http
        }
        mWebView.setHorizontalScrollBarEnabled(false);//去掉webview的滚动条,水平不显示
        mWebView.setScrollbarFadingEnabled(true);//不活动的时候隐藏，活动的时候显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false); //自动播放音乐
        }
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);//设置滚动条样式
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER); // 取消WebView中滚动或拖动到顶部、底部时的阴影
        mWebView.requestFocus(); // 触摸焦点起作用
    }

    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private void initListener() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading1: url=" + url);
                view.loadUrl(url);
                return true;
            }

            //在网页上的所有加载都经过这个方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                super.shouldOverrideUrlLoading(view, request);
                String url = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    url = request.getUrl().toString();
                } else {
                    url = request.toString();
                }
                Log.i(TAG, "shouldOverrideUrlLoading2: url=" + url);
                if (url.startsWith("http://") || url.startsWith("https://")) { //加载的url是http/https协议地址
                    view.loadUrl(url);
                    return false; //返回false表示此url默认由系统处理,url未加载完成，会继续往下走
                } else { //加载的url是自定义协议地址
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        mActivity.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);//开始加载
                Log.i(TAG, "开始载入页面调用onPageStarted: " + url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.i(TAG, "加载资源时响应onLoadResource: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mProgressBar.setVisibility(View.GONE);//加载错误页面
                Log.i(TAG, error.toString() + "加载页面的服务器出现错误时onReceivedError: " + request.toString());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);//加载结束
                Log.i(TAG, "在页面加载结束时调用onPageFinished: " + url);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                Log.i(TAG, realm + "获取返回信息授权请求onReceivedHttpAuthRequest: " + handler.toString());
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();    //表示等待证书响应---接受所有网站的证书，忽略SSL错误，执行访问网页
                Log.i(TAG, error.toString() + "处理https请求onReceivedSslError: " + handler.toString());
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                Log.i(TAG, "onShowCustomView: ");
                mCustomViewCallback = callback;
                if (contentParentView.getVisibility() == INVISIBLE) {
                    contentParentView.addView(view);
                    contentParentView.setVisibility(VISIBLE);
                    requestHorizontalScreen();
                    contentParentView.bringToFront();
                }
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                Log.i(TAG, "onHideCustomView: ");
                mCustomViewCallback = null;
                if (contentParentView.getVisibility() == VISIBLE) {
                    contentParentView.setVisibility(INVISIBLE);
                    contentParentView.removeAllViews();
                    requestVerticalScreen();
                }
            }

            //拦截确认框
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            //拦截输入框
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            //拦截警告框
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            //获取网页title
            @Override
            public void onReceivedTitle(WebView view, String title) {
                Log.i(TAG, "onReceivedTitle: 网页标题 title=" + title);
            }

            //获得网页的加载进度并显示
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 100) {
                    mProgressBar.setVisibility(View.GONE);//加载到100
                } else {
                    if (mProgressBar.getVisibility() == View.GONE) {
                        mProgressBar.setVisibility(View.VISIBLE);//加载中
                    }
                    mProgressBar.setProgress(newProgress);//更新进度
                }
            }
        });
        //JsToAndroid类对象映射到js的jsToAndroid对象 js中调用<android.send();>  Android将接受到消息
        mWebView.addJavascriptInterface(new JsToAndroid(new JsToAndroid.OnReceiveListener() {
            @Override
            public void onReceive(String msg) {
                Log.i(TAG, "onReceive: msg=" + msg);
                if (onReceiveListener != null) {
                    onReceiveListener.onReceive(msg);
                }
            }
        }), "android");
    }


    //加载网页
    public void loadUrl(String url) {
        if (mWebView != null) {
            mWebView.loadUrl(url);
        } else {
            Log.i(TAG, "loadUrl: mWebView未加载完成");
        }
    }

    //回退网页
    public boolean goBack() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    public void onPause() {
        if (mWebView == null)
            return;
        mWebView.onPause();
        mWebView.pauseTimers();
        mWebView.getSettings().setJavaScriptEnabled(false);//取消支持js
    }

    public void onResume() {
        if (mWebView == null)
            return;
        mWebView.onResume();
        mWebView.resumeTimers();
        mWebView.getSettings().setJavaScriptEnabled(true);//支持js
    }

    public void onDestroy() {
        if (mWebView == null)
            return;
        removeView(mWebView);
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.stopLoading();
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    public WebView getmWebView() {
        return mWebView;
    }

    public String getUrl() {
        return mUrl;
    }

    public WebSettings getSettings() {
        return settings;
    }

    //Js发送到Android的消息用这个接受
    public void setOnReceiveJSMsgListener(JsToAndroid.OnReceiveListener onReceiveListener) {
        this.onReceiveListener = onReceiveListener;
    }

    //按下返回键时 监听是否已经被webview消费
    public boolean isKeyDownConsumeBack(int keyCode) {
        if (mWebView == null)
            return false;
        // 是否按下返回键，且WebView现在的层级，可以返回
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (contentParentView.getVisibility() == VISIBLE) {//如果在全屏播放先退出全屏播放
                if (mCustomViewCallback != null)
                    mCustomViewCallback.onCustomViewHidden();
                return true;
            }
            return goBack();
        }
        return false;
    }

    public void requestVerticalScreen() {//请求竖屏
        if (mActivity != null) {//竖屏
            if (!isActivityFullScreen) {//activity如果是全屏就不设置全屏和取消全屏
                setFullScreen(mActivity, false);//取消全屏
            }
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public void requestHorizontalScreen() {//请求横屏
        if (mActivity != null) {//横屏
            if (!isActivityFullScreen) {//activity如果是全屏就不设置全屏和取消全屏
                setFullScreen(mActivity, true);//全屏
            }
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    private void setFullScreen(AppCompatActivity activity, boolean enable) {
        if (enable) {//全屏
            activity.getSupportActionBar().hide();//AppCompatActivity去标题栏
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(attrs);
        } else {//取消全屏
            activity.getSupportActionBar().show();//AppCompatActivity显示标题栏
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(attrs);
        }
    }

    //Android调用js方法
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void androidToJs(String script, final OnAndroidToJsCallBack androidToJsCallBack) {
        if (mWebView == null)
            return;
        if (Build.VERSION.SDK_INT < 18) {//18对应Android4.3
            mWebView.loadUrl(script);
            if (androidToJsCallBack != null) {
                androidToJsCallBack.onReceiveValue("");//Android版本不小于4.3，可以接受返回值
            }
        } else {
            mWebView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.i(TAG, "onReceiveValue: value" + value);
                    //此处为 js 返回的结果
                    if (androidToJsCallBack != null) {
                        androidToJsCallBack.onReceiveValue(value);//Android版本不小于4.3，可以接受返回值
                    }
                }
            });
        }
    }

    public interface OnAndroidToJsCallBack {
        void onReceiveValue(String value);//接受返回值
    }

    public static class JsToAndroid extends Object {
        private String TAG = getClass().getName() + "--------->";

        private OnReceiveListener onReceiveListener;

        public JsToAndroid(OnReceiveListener onReceiveListener) {
            this.onReceiveListener = onReceiveListener;
        }

        // 定义JS需要调用的方法
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        public void send(String msg) {
            Log.i(TAG, "send: msg=" + msg);
            if (onReceiveListener != null) {
                onReceiveListener.onReceive(msg);
            }
        }

        public interface OnReceiveListener {
            void onReceive(String msg);
        }
    }
}
