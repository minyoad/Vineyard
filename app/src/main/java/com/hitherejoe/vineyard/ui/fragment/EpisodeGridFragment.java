package com.hitherejoe.vineyard.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.ui.activity.DetailsActivity;
import com.hitherejoe.vineyard.ui.activity.XwalkWebViewActivity;
import com.hitherejoe.vineyard.ui.presenter.EpisodePresenter;

import java.util.List;

import timber.log.Timber;

/**
 * EpisodeGridFragment shows contents with vertical alignment
 */
public class EpisodeGridFragment extends android.support.v17.leanback.app.VerticalGridFragment {

    private static final String TAG = EpisodeGridFragment.class.getSimpleName();
    private static final int NUM_COLUMNS = 1;

    private static final String MOVIE="MOVIE";
    private static final String SOURCE_TYPE="SOURCE_TYPE";

    public static final int SOURCE_TYPE_SOURCE=0;
    public static final int SOURCE_TYPE_EPISODE=1;


    private ArrayObjectAdapter mAdapter;

    private Movie mMovie;

    private int mSourceType;
    private int mSelectedPosition;

    public static EpisodeGridFragment newInstance(Movie movie,int sourceType){
        EpisodeGridFragment episodeGridFragment= new EpisodeGridFragment();

        Bundle bundle=new Bundle();
        bundle.putParcelable(MOVIE,movie);
        bundle.putInt(SOURCE_TYPE,sourceType);

        episodeGridFragment.setArguments(bundle);

        return episodeGridFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

//        setTitle("VerticalGridFragment");
        //setBadgeDrawable(getResources().getDrawable(R.drawable.app_icon_your_company));

//        setupEventListeners();
        setupFragment();


        mSelectedPosition=mMovie.currentIndex;
        // it will move current focus to specified position. Comment out it to see the behavior.
        setSelectedPosition(mMovie.currentIndex);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        Bundle bundle=getArguments();

        mMovie=bundle.getParcelable(MOVIE);

        mSourceType=bundle.getInt(SOURCE_TYPE);


    }

    @Override
    public void setSelectedPosition(int position){
        super.setSelectedPosition(position);

        for(int i=0;i<mAdapter.size();i++){
            EpisodePresenter presenter=(EpisodePresenter) mAdapter.getPresenter(mAdapter.get(i));

            presenter.setSelected(i==position);


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getFocus();
    }
    //主界面获取焦点
    private void getFocus() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();

        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Timber.d("onKey,keycode="+keyCode);

                if(event.getAction()==KeyEvent.ACTION_UP){
                    switch (keyCode){
                        case KeyEvent.KEYCODE_DPAD_UP:{
                            setSelectedPosition(mSelectedPosition--);

                        }
                        break;
                        case  KeyEvent.KEYCODE_DPAD_DOWN:{
                            setSelectedPosition(mSelectedPosition++);

                        }
                        break;
                        case KeyEvent.KEYCODE_DPAD_CENTER:{
                            playUrlOnPosition(mSelectedPosition);

                        }
                        break;
                    }
                }

//                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//                    // 监听到返回按钮点击事件
//
//                    return true;
//                }
                return false;
            }
        });
    }

    private void playUrlOnPosition(int selectedPosition) {

        if(mSourceType==SOURCE_TYPE_EPISODE){
            Movie.PlayUrlInfo playUrlInfo=(Movie.PlayUrlInfo) mAdapter.get(selectedPosition);
            Intent intent = new Intent(getActivity(), XwalkWebViewActivity.class);
            intent.putExtra(DetailsActivity.MOVIE, mMovie);
            intent.putExtra("URL",playUrlInfo.url);
            getActivity().startActivity(intent);
        }
        else{
            String sourceName=(String)mAdapter.get(selectedPosition);
            mMovie.currentSource=sourceName;

            Intent intent = new Intent(getActivity(), XwalkWebViewActivity.class);
            intent.putExtra(DetailsActivity.MOVIE, mMovie);
            intent.putExtra("URL",mMovie.getVideoUrlInfo(sourceName,mMovie.currentIndex).url);
            getActivity().startActivity(intent);
        }
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);



        mAdapter = new ArrayObjectAdapter(new EpisodePresenter());

        if (mSourceType==0){
            List<String> sourceList=mMovie.getPlaySrcList();

            mAdapter.addAll(0,sourceList);
        }else {
            List<Movie.PlayUrlInfo> urlList=mMovie.getPlayUrlList(mMovie.currentSource);
            mAdapter.addAll(0,urlList);
        }

        setAdapter(mAdapter);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if(item instanceof Movie.PlayUrlInfo){
                Movie.PlayUrlInfo playUrlInfo=(Movie.PlayUrlInfo)item;
                Intent intent = new Intent(getActivity(), XwalkWebViewActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, mMovie);
                intent.putExtra("URL",playUrlInfo.url);
                getActivity().startActivity(intent);
            }
            else{
                String sourceName=(String)item;
                mMovie.currentSource=sourceName;

                Intent intent = new Intent(getActivity(), XwalkWebViewActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, mMovie);
                intent.putExtra("URL",mMovie.getVideoUrlInfo(sourceName,mMovie.currentIndex).url);
                getActivity().startActivity(intent);
            }

        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if( item != null ){
//                picassoBackgroundManager.updateBackgroundWithDelay(((Movie) item).getBackgroundImageUrl());
            } else {
                Log.w(TAG, "item is null");
            }

        }
    }
}