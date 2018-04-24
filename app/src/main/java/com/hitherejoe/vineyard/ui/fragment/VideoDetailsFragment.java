package com.hitherejoe.vineyard.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.app.DetailsFragmentBackgroundController;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hitherejoe.vineyard.R;
//import com.hitherejoe.vineyard.common.Utils;
//import com.hitherejoe.vineyard.data.VideoProvider;
import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Movie;
//import com.hitherejoe.vineyard.ui.background.PicassoBackgroundManager;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.XwalkWebViewActivity;
import com.hitherejoe.vineyard.ui.adapter.CustomListRow;
import com.hitherejoe.vineyard.ui.presenter.CardPresenter;
import com.hitherejoe.vineyard.ui.presenter.CustomDetailsOverviewRowPresenter;
import com.hitherejoe.vineyard.ui.presenter.CustomFullWidthDetailsOverviewRowPresenter;
import com.hitherejoe.vineyard.ui.presenter.DetailsDescriptionPresenter;
import com.hitherejoe.vineyard.ui.activity.DetailsActivity;
import com.hitherejoe.vineyard.ui.presenter.EpisodePresenter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by corochann on 6/7/2015.
 */
public class VideoDetailsFragment extends DetailsFragment {

    private static final String TAG = VideoDetailsFragment.class.getSimpleName();

    public static final String TRANSITION_NAME = "t_for_transition";

    @Inject DataManager mDataManager;


    private static final int ACTION_PLAY_VIDEO = 1;
    private static final int ACTION_SHOW_EPISODE = 2;
    private static final int ACTION_SHOW_RELATED = 3;
    private static final int ACTION_ADD_FAVOURITE = 4;


    private static final int FULL_WIDTH_DETAIL_THUMB_WIDTH = 220;
    private static final int FULL_WIDTH_DETAIL_THUMB_HEIGHT = 120;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    public static final String CATEGORY_FULL_WIDTH_DETAILS_OVERVIEW_ROW_PRESENTER = "FullWidthDetailsOverviewRowPresenter";
    public static final String CATEGORY_DETAILS_OVERVIEW_ROW_PRESENTER = "DetailsOverviewRowPresenter";



    /* Attribute */
    private ArrayObjectAdapter mAdapter;
    private CustomFullWidthDetailsOverviewRowPresenter mFwdorPresenter;
    private CustomDetailsOverviewRowPresenter mDorPresenter;
    private ClassPresenterSelector mClassPresenterSelector;
    private ListRow mRelatedVideoRow = null;

    private DetailsRowBuilderTask mDetailsRowBuilderTask;

//    private Drawable mDefaultCardImage;


    /* Relation */
    private Movie mSelectedMovie;
    private List<Movie> mVideoLists = null;
    private Subscription mCategorySubcription;


    private final DetailsFragmentBackgroundController mDetailsBackground =
            new DetailsFragmentBackgroundController(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

//        mDefaultCardImage = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_card_default);

        mDataManager = VineyardApplication.get(getActivity()).getComponent().dataManager();

        mFwdorPresenter = new CustomFullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        mDorPresenter = new CustomDetailsOverviewRowPresenter(new DetailsDescriptionPresenter(), getActivity());

        mSelectedMovie = getActivity().getIntent().getParcelableExtra(DetailsActivity.MOVIE);

        setTitle(mSelectedMovie.getTitle());


        mDetailsRowBuilderTask = (DetailsRowBuilderTask) new DetailsRowBuilderTask().execute(mSelectedMovie);

        setOnItemViewClickedListener(new ItemViewClickedListener());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mClassPresenterSelector = new ClassPresenterSelector();
        Log.v(TAG, "mFwdorPresenter.getInitialState: " + mFwdorPresenter.getInitialState());

        FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getActivity(), TRANSITION_NAME);
        mFwdorPresenter.setListener(mHelper);
        mFwdorPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();

//        if(mSelectedMovie.getCategory().equals(CATEGORY_DETAILS_OVERVIEW_ROW_PRESENTER)) {
//            /* If category name is "DetailsOverviewRowPresenter", show DetailsOverviewRowPresenter for demo purpose (this class is deprecated from API level 22) */
//            mClassPresenterSelector.addClassPresenter(DetailsOverviewRow.class, mDorPresenter);
//        } else {
//            /* Default behavior, show FullWidthDetailsOverviewRowPresenter */
//            mClassPresenterSelector.addClassPresenter(DetailsOverviewRow.class, mFwdorPresenter);
//        }

        mClassPresenterSelector.addClassPresenter(DetailsOverviewRow.class, mFwdorPresenter);

        mClassPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mAdapter = new ArrayObjectAdapter(mClassPresenterSelector);

        setAdapter(mAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startEntranceTransition();
            }
        }, 500);
        initializeBackground();

        mVideoLists=new LinkedList<>();

    }

    private void initializeBackground() {
        mDetailsBackground.enableParallax();
        mDetailsBackground.setCoverBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.background_canyon));
    }

    @Override
    public void onStop() {
        mDetailsRowBuilderTask.cancel(true);

        if (mCategorySubcription!=null && !mCategorySubcription.isUnsubscribed())
        mCategorySubcription.unsubscribe();

        super.onStop();
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);

                getActivity().startActivity(intent);

            }
            else if(item instanceof Movie.PlayUrlInfo){
                Movie.PlayUrlInfo playUrlInfo=(Movie.PlayUrlInfo)item;

                Intent intent = new Intent(getActivity(), XwalkWebViewActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
                intent.putExtra(DetailsActivity.PLAY_URL, playUrlInfo.url);


//                intent.putExtra(getResources().getString(R.string.should_start), true);
                startActivity(intent);
            }
        }
    }

    protected void getRelatedVideos(){

        if (mDataManager==null){
            Timber.e("mdatamanager null");
            return;
        }

        Observable<VineyardService.MovieResponse> observable=mDataManager.getRelatedMovies(mSelectedMovie);

        //TODO: Handle error
//                        adapter.removeLoadingIndicator();
//                        Timber.e("There was an error loading the videos", e);
        mCategorySubcription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.MovieResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //TODO: Handle error
//                        adapter.removeLoadingIndicator();
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.error_message_retrieving_results),
                                Toast.LENGTH_SHORT
                        ).show();
//                        Timber.e("There was an error loading the videos", e);
                    }

                    @Override
                    public void onNext(VineyardService.MovieResponse movieResponse) {
                        mVideoLists.addAll(movieResponse.data);


                        if (mVideoLists == null ||mVideoLists.isEmpty()) {
                            // Error occured while fetching videos
                            Log.i(TAG, "mVideoLists is null, skip creating mRelatedVideoRow");
                        } else {
                            CardPresenter cardPresenter = new CardPresenter(getActivity());

                            ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);

                            for (int j = 0; j < mVideoLists.size(); j++) {
                                cardRowAdapter.add(mVideoLists.get(j));
                            }
                            //HeaderItem header = new HeaderItem(index, entry.getKey());
                            HeaderItem header = new HeaderItem(0, "看过的人也喜欢");
                            mRelatedVideoRow = new ListRow(header, cardRowAdapter);


                                  /* 3nd row */
                            if(mRelatedVideoRow != null){
                                mAdapter.add(mRelatedVideoRow);
                            }
//                }
                        }

                    }
                });
    }

    private class DetailsRowBuilderTask extends AsyncTask<Movie, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground(Movie... params) {
            Log.v(TAG, "DetailsRowBuilderTask doInBackground");
            int width, height;
//            if(mSelectedMovie.getCategory().equals(CATEGORY_DETAILS_OVERVIEW_ROW_PRESENTER)) {
//                /* If category name is "DetailsOverviewRowPresenter", show DetailsOverviewRowPresenter for demo purpose (this class is deprecated from API level 22) */
//                width = DETAIL_THUMB_WIDTH;
//                height = DETAIL_THUMB_HEIGHT;
//            } else {
                /* Default behavior, show FullWidthDetailsOverviewRowPresenter */
                width = FULL_WIDTH_DETAIL_THUMB_WIDTH;
                height = FULL_WIDTH_DETAIL_THUMB_HEIGHT;
//            }

            DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);

            try {
                // Bitmap loading must be done in background thread in Android.

                Bitmap poster= Glide.with(getActivity())
                        .load(mSelectedMovie.getCardImageUrl())
                        .asBitmap()
                        .into(width,height)
//                        .error(mDefaultCardImage)
                        .get();


                row.setImageBitmap(getActivity(), poster);

                getRelatedVideos();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return row;
        }



        @Override
        protected void onPostExecute(DetailsOverviewRow row) {
            Log.v(TAG, "DetailsRowBuilderTask onPostExecute");
            /* 1st row: DetailsOverviewRow */

            List<Movie.PlayUrlInfo> playUrlInfoList=mSelectedMovie.getPlayUrlMap().get(mSelectedMovie.currentSource);

              /* action setting*/
            SparseArrayObjectAdapter sparseArrayObjectAdapter = new SparseArrayObjectAdapter();

            sparseArrayObjectAdapter.set(ACTION_PLAY_VIDEO, new Action(ACTION_PLAY_VIDEO, "播放"));

            if (playUrlInfoList.size()>1)
            sparseArrayObjectAdapter.set(ACTION_SHOW_EPISODE, new Action(ACTION_SHOW_EPISODE, "选择分集", ""));

            sparseArrayObjectAdapter.set(ACTION_SHOW_RELATED, new Action(ACTION_SHOW_RELATED, "相关视频", ""));


            sparseArrayObjectAdapter.set(ACTION_ADD_FAVOURITE, new Action(ACTION_ADD_FAVOURITE, "加入收藏", ""));


            row.setActionsAdapter(sparseArrayObjectAdapter);

            mFwdorPresenter.setOnActionClickedListener(new DetailsOverviewRowActionClickedListener());
            mDorPresenter.setOnActionClickedListener(new DetailsOverviewRowActionClickedListener());

            /* 1st row */
            mAdapter.add(row);


            /* 2nd row: ListRow CardPresenter */

//            ArrayObjectAdapter episodeAapter=new ArrayObjectAdapter(new CustomListRowPresenter());
            if (playUrlInfoList.size()>1) {
                // add episode list
                EpisodePresenter episodePresenter = new EpisodePresenter();

                ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(episodePresenter);


                for (Movie.PlayUrlInfo playUrlInfo : playUrlInfoList) {
                    cardRowAdapter.add(playUrlInfo);
                }

                HeaderItem header = new HeaderItem(0, "分集列表");
                CustomListRow episodeRow = new CustomListRow(header, cardRowAdapter);
                episodeRow.setNumRows(10);

                mAdapter.add(episodeRow);
            }


            /* 3rd row */
            //adapter.add(new ListRow(headerItem, listRowAdapter));

            //
            setAdapter(mAdapter);
        }
    }

    public class DetailsOverviewRowActionClickedListener implements OnActionClickedListener {
        @Override
        public void onActionClicked(Action action) {

            switch ((int) action.getId()){
                case ACTION_PLAY_VIDEO:{
                    Intent intent = new Intent(getActivity(), XwalkWebViewActivity.class);
                    intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
//                intent.putExtra(getResources().getString(R.string.should_start), true);
                    startActivity(intent);
                }
                break;
                case ACTION_SHOW_EPISODE:{
                    setSelectedPosition(1);

                }
                break;
                case ACTION_SHOW_RELATED:{
                    setSelectedPosition(2);
                }
                break;

                case ACTION_ADD_FAVOURITE:{
                    //add fav
                }
                break;
            }

        }
    }
}