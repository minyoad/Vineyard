package com.hitherejoe.vineyard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

//        @Override
//        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
//            Timber.d("request:"+url);
//
//            return super.shouldInterceptLoadRequest(view, url);
//        }
    }

    class MyUIClient extends XWalkUIClient {
        MyUIClient(XWalkView view) {
            super(view);
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


        String url=movie.getVideoUrl();

        String proxy=movie.getProxyUrlByPlayer(movie.currentSource);


        Timber.d("URL="+proxy+url);
        mXwalkView.load(proxy+url, null);
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
