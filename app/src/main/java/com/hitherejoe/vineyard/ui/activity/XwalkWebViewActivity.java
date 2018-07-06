package com.hitherejoe.vineyard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.local.PlayerHelper;
import com.hitherejoe.vineyard.data.model.Movie;
import com.mybacc.popupmenu.MenuItem;
import com.mybacc.popupmenu.PopupMenu;


import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class XwalkWebViewActivity extends AppCompatActivity {
    XWalkView mXwalkView;

    @Bind(R.id.main_image)
    ImageView mImageView;

    @Bind(R.id.view_overlay)
    View mOverlayView;

    @Bind(R.id.loadingLayout)
    LinearLayout mLinearLayout;

    @Bind(R.id.progress_card)
    ProgressBar mProgressCard;

    @Bind(R.id.movieName)
    TextView mMovieName;

    @Bind(R.id.episodeName)
    TextView mEpisodeName;

    @Bind(R.id.episode_list_containter)
    View mEpisodeListView;

    @Bind(R.id.episode_source_containter)
    View mEpisodeSourceView;


    boolean paused;
    private Movie mMovie;

    private boolean mLoading;

    private PopupMenu mSourceMenu, mEpisodeMenu;

    class MyResourceClient extends XWalkResourceClient {
        MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
            Timber.d("request:" + request.getUrl());

            if (request.getUrl().toString().contains("js.zyrfanli.com")) {
                return createXWalkWebResourceResponse("", "", null);
            } else if (request.getUrl().toString().contains("cnzz.com/")) {
                return createXWalkWebResourceResponse("", "", null);
            } else if (request.getUrl().toString().contains("img2.xjoot.com")) {
                return createXWalkWebResourceResponse("", "", null);
            } else if (request.getUrl().toString().contains("km.jianduankm.com")) {
                return createXWalkWebResourceResponse("", "", null);
            } else if (request.getUrl().toString().contains("e.nirentang.com")) {
                return createXWalkWebResourceResponse("", "", null);
            }
//            else if (request.getUrl().toString().contains("boba.52kuyun.com/html/js/share.js")) {
//                return createXWalkWebResourceResponse("", "", null);
//            }

            return super.shouldInterceptLoadRequest(view, request);

        }


    }

    class MyUIClient extends XWalkUIClient {

        XWalkView mWalkView;

        private void pause() {
            String js = "javascript:getVideo().pause();";

            mWalkView.loadUrl(js);
        }

        private void forward() {
            String js = "javascript:getVideo().pause();getVideo().currentTime+=10;getVideo().play()";
            mWalkView.loadUrl(js);
        }

        private void backward() {
            String js = "javascript:getVideo().pause();getVideo().currentTime-=10;getVideo().play()";
            mWalkView.loadUrl(js);
        }

        private void play() {
            String js = "javascript:getVideo().play();";
            mWalkView.loadUrl(js);
        }

        private void muted(){
            String js = "javascript:getVideo().muted=true;";
            mWalkView.loadUrl(js);
        }



        MyUIClient(XWalkView view) {
            super(view);
            mWalkView = view;
        }


        private void injectScriptFile(XWalkView view, String scriptFile) {
            InputStream input;
            try {
                input = getAssets().open(scriptFile);
                byte[] buffer = new byte[input.available()];
                input.read(buffer);
                input.close();

                // String-ify the script byte-array using BASE64 encoding !!!
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                view.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type = 'text/javascript';" +
                        // Tell the browser to BASE64-decode the string into your script !!!
                        "script.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(script)" +
                        "})()");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);

            injectScriptFile(view, "script.js");
            view.loadUrl("javascript:init();");

            muted();

            play();


        }

        @Override
        public boolean shouldOverrideKeyEvent(XWalkView view, KeyEvent event) {

            Log.d("XwalkWebView", "KEYEVENT:=" + event.getKeyCode());

            boolean overrided = true;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP: {//向上
                }
                break;
                case KeyEvent.KEYCODE_DPAD_DOWN: {//向下
                    showEpisodeList();
                }
                break;
                case KeyEvent.KEYCODE_DPAD_LEFT: {//向左
                    backward();
                }
                break;
                case KeyEvent.KEYCODE_DPAD_RIGHT: {//向右
                    forward();
                }
                break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER: {//确定

                    if (paused)
                        play();
                    else
                        pause();
                }
                break;
                case KeyEvent.KEYCODE_BACK: {//返回

//                    if(mEpisodeListFragment!=null && mEpisodeListFragment.isVisible()){
//                        getFragmentManager().beginTransaction()
//                                .remove(mEpisodeListFragment);
//                    }
//                    else if(mEpisodeSourceFragment!=null &&mEpisodeSourceFragment.isVisible()){
//                        getFragmentManager().beginTransaction()
//                                .remove(mEpisodeSourceFragment);
//                    }
//                    else{
////                        Intent intent = new Intent(getBaseContext(),DetailsActivity.class);
////                        intent.putExtra(DetailsActivity.MOVIE, mMovie);
////                        startActivity(intent);
//                        finish();
//                    }


                }
                break;
                case KeyEvent.KEYCODE_HOME: {//房子

                }
                break;
                case KeyEvent.KEYCODE_MENU: {//菜单
                    showSourceList();

                }
                break;

                default: {
                    overrided = super.shouldOverrideKeyEvent(view, event);
                }


            }

            return overrided;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xwalkwebview);
        ButterKnife.bind(this);
        mXwalkView = findViewById(R.id.xwalkView);
        mXwalkView.setResourceClient(new MyResourceClient(mXwalkView));
        mXwalkView.setUIClient(new MyUIClient(mXwalkView));


        XWalkSettings settings = mXwalkView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptEnabled(true);


        mXwalkView.addJavascriptInterface(new JSVideoObj(), "videoObj");

        Intent intent = getIntent();

        mMovie = intent.getParcelableExtra(DetailsActivity.MOVIE);

        String url = intent.getStringExtra(DetailsActivity.PLAY_URL);

        int pid = 0;

        if (url != null && !url.isEmpty()) {

            pid = mMovie.getIndexByUrl(url);

        }


        play(pid);


    }

    public void play(int pid) {

        mMovie.currentIndex = pid;

        Movie.PlayUrlInfo playUrlInfo = mMovie.getPlayUrl(mMovie.currentSource, pid);

        String url = playUrlInfo.url;

        String proxy = mMovie.getProxyUrlByPlayer(mMovie.currentSource);

        Timber.d("URL=" + proxy + url);
        mXwalkView.loadUrl(proxy + url, null);

        showLoadingView();


    }

    public void showLoadingView() {

        mMovieName.setText(mMovie.getTitle());
        mEpisodeName.setText(mMovie.getPlayUrl(mMovie.currentSource, mMovie.currentIndex).title);

        mImageView.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.VISIBLE);
        mOverlayView.setVisibility(View.VISIBLE);
        mProgressCard.setVisibility(View.VISIBLE);

        mLoading = true;

    }

    private void unmuted(){
        String js = "javascript:getVideo().muted=false;";
        mXwalkView.loadUrl(js);
    }
    public void hideLoadingView() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI

                mImageView.setVisibility(View.INVISIBLE);
                mLinearLayout.setVisibility(View.INVISIBLE);
                mOverlayView.setVisibility(View.INVISIBLE);
                mProgressCard.setVisibility(View.INVISIBLE);
                unmuted();
            }
        });

        mLoading = false;


    }


    public void showSourceList() {
        Timber.d("movie=" + mMovie);

        if (mSourceMenu != null) {
            mSourceMenu.dismiss();
            mSourceMenu = null;
        }

        PopupMenu menu = new PopupMenu(this);
        menu.setHeaderTitle("视频源");
        final List<String> srcList = mMovie.getPlaySrcList();

        // Set Listener
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {
                int idx = item.getItemId();
                String sourceName = srcList.get(idx);
                mMovie.currentSource = sourceName;

                Intent intent = new Intent(getBaseContext(), XwalkWebViewActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, mMovie);
                intent.putExtra("URL", mMovie.getVideoUrlInfo(sourceName, mMovie.currentIndex).url);
                startActivity(intent);

            }
        });

        PlayerHelper playerHelper = VineyardApplication.get(this).getComponent().playerHelper();
        int i = 0;
        for (String src : srcList) {
            menu.add(i, playerHelper.getPlayerName(src));
            i++;
        }

        int idx = srcList.indexOf(mMovie.currentSource);

        mSourceMenu = menu;

        menu.show(mEpisodeSourceView);

        menu.setSelectedPosition(idx);

    }

    public void showEpisodeList() {

        if (mEpisodeMenu != null) {
            mEpisodeMenu.dismiss();
            mEpisodeMenu = null;
        }

        PopupMenu menu = new PopupMenu(this);
        menu.setHeaderTitle("分集列表");
        final List<Movie.PlayUrlInfo> urlInfoList = mMovie.getPlayUrlList(mMovie.currentSource);

        // Set Listener
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MenuItem item) {

                int idx = item.getItemId();

                Movie.PlayUrlInfo urlInfo = urlInfoList.get(idx);

                Intent intent = new Intent(getBaseContext(), XwalkWebViewActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, mMovie);
                intent.putExtra("URL", urlInfo.url);
                startActivity(intent);

            }
        });

        int i = 0;
        for (Movie.PlayUrlInfo urlInfo : urlInfoList) {

            MenuItem menuItem = menu.add(i, urlInfo.title);

            if (i == mMovie.currentIndex) {
                menuItem.setIcon(getDrawable(R.drawable.ic_play));
            }

            i++;
        }
        mEpisodeMenu = menu;
//        }

        menu.show(mEpisodeListView);
        mEpisodeMenu.setSelectedPosition(mMovie.currentIndex);


    }

    public class JSVideoObj {

        private final String TAG = JSVideoObj.class.getSimpleName();

        @org.xwalk.core.JavascriptInterface
        public void processEvents(String eventType) {
            Timber.d("event:" + eventType);

            switch (eventType) {
                case "ended": {
                    //play next video


                    if (!mMovie.isLastEpisode())

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                play(mMovie.currentIndex+1);

                            }
                        });

                }
                break;
                case "progress":
                case "canplay":
                case "playing": {

                    //hide loading view
//                    if(mLoading) {
//                        hideLoadingView();
//                    }

                }
            }

        }

        @org.xwalk.core.JavascriptInterface
        public void processProperties(String propertiesJson) {

//            Timber.d("processsProperties:" + propertiesJson);

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(propertiesJson);

                if (jsonObject != null) {

                    double currentTime = jsonObject.getDouble("currentTime");

                    Timber.d("currentTime:" + currentTime);

                    int intTime = (int) currentTime;

                    if (intTime > 0 && mLoading) {

                        hideLoadingView();

                    }


                    paused = jsonObject.getBoolean("paused");
                }

            } catch (JSONException e) {
                Log.e(TAG, "processsProperties: " + e.getMessage());
            }


        }

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
