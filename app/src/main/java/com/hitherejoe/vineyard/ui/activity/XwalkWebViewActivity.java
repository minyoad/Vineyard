package com.hitherejoe.vineyard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Movie;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import timber.log.Timber;

public class XwalkWebViewActivity extends AppCompatActivity {
    XWalkView mXwalkView;

    View loadingView;
    class MyResourceClient extends XWalkResourceClient {
        MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
            Timber.d("request:"+request.getUrl());

            if (request.getUrl().toString().contains("js.zyrfanli.com")){
                return createXWalkWebResourceResponse("","",null);
            }

            return super.shouldInterceptLoadRequest(view, request);

        }


    }

    class MyUIClient extends XWalkUIClient {

        XWalkView mWalkView;
        private void pause(){
            String js="javascript:var v = document.getElementsByTagName(\"video\")[0];\n" +
                    "v.paused?v.play():v.pause();";


            mWalkView.loadUrl(js);
        }

        private void forward(){

        }

        private void backward(){

        }

        private void play(){
            String js="javascript:var v = document.getElementsByTagName(\"video\")[0];\n" +
                    "v.play();";
            mWalkView.loadUrl(js);
        }



        MyUIClient(XWalkView view) {
            super(view);
            mWalkView=view;
        }


        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);

            play();

            loadingView.setVisibility(View.GONE);

        }

        @Override
        public boolean shouldOverrideKeyEvent(XWalkView view, KeyEvent event) {

            boolean overrided=true;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP://向上
//                    Log.e("jamie","－－－－－向上－－－－－");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN://向下
//                    Log.e("jamie","－－－－－向下－－－－－");
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT://向左
//                    Log.e("jamie","－－－－－向左－－－－－");
                    backward();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT://向右
//                    Log.e("jamie","－－－－－向右－－－－－");
                    forward();
                    break;
                case KeyEvent.KEYCODE_ENTER://确定
//                    Log.e("jamie","－－－－－确定－－－－－");
                    pause();

                    break;
                case KeyEvent.KEYCODE_BACK://返回
//                    Log.e("jamie","－－－－－返回－－－－－");
                    break;
                case KeyEvent.KEYCODE_HOME://房子
//                    Log.e("jamie","－－－－－房子－－－－－");
                    break;
                case KeyEvent.KEYCODE_MENU://菜单
//                    Log.e("jamie","－－－－－菜单－－－－－");
                    break;

                    default:{
                        overrided=super.shouldOverrideKeyEvent(view, event);;
                    }


            }

            return overrided;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xwalkwebview);
        mXwalkView = (XWalkView) findViewById(R.id.xwalkView);
        mXwalkView.setResourceClient(new MyResourceClient(mXwalkView));
        mXwalkView.setUIClient(new MyUIClient(mXwalkView));


        XWalkSettings settings = mXwalkView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptEnabled(true);

        Intent intent=getIntent();
        Movie movie=intent.getParcelableExtra(DetailsActivity.MOVIE);

        String url=intent.getStringExtra(DetailsActivity.PLAY_URL);

        if(url==null ||url.isEmpty())
            url=movie.getVideoUrl();

        String proxy=movie.getProxyUrlByPlayer(movie.currentSource);


        Timber.d("URL="+proxy+url);
        mXwalkView.loadUrl(proxy+url, null);


        loadingView=findViewById(R.id.loadingPanel);
        loadingView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mXwalkView != null) {
            mXwalkView.pauseTimers();
            mXwalkView.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mXwalkView != null) {
            mXwalkView.resumeTimers();
            mXwalkView.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXwalkView != null) {
            mXwalkView.onDestroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mXwalkView != null) {
            mXwalkView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mXwalkView != null) {
            mXwalkView.onNewIntent(intent);
        }
    }
}
