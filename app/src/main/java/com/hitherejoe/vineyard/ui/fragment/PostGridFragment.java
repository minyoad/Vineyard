package com.hitherejoe.vineyard.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Option;
import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.adapter.PaginationAdapter;
import com.hitherejoe.vineyard.ui.adapter.MovieAdapter;
import com.hitherejoe.vineyard.util.NetworkUtil;
import com.hitherejoe.vineyard.util.ToastFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PostGridFragment extends VerticalGridFragment {

    public static final String ARG_ITEM = "arg_item";
    public static final String TYPE_USER = "user";
    public static final String TYPE_TAG = "tag";

    @Inject CompositeSubscription mCompositeSubscription;
    @Inject DataManager mDataManager;

    private static final int NUM_COLUMNS = 5;
    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private Handler mHandler;
    private MovieAdapter mPostAdapter;
    private Runnable mBackgroundRunnable;
    private String mSelectedType;
    private boolean mIsStopping;

    public static PostGridFragment newInstance(Object selectedItem) {
        PostGridFragment postGridFragment = new PostGridFragment();
        Bundle args = new Bundle();
        if (selectedItem instanceof User) {
            args.putParcelable(ARG_ITEM, (User) selectedItem);
        } else if (selectedItem instanceof Tag) {
            args.putParcelable(ARG_ITEM, (Tag) selectedItem);
        }
        postGridFragment.setArguments(args);
        return postGridFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        Bundle args = getArguments();
        Object item = args.getParcelable(ARG_ITEM);
        if (item == null) {
            throw new IllegalArgumentException("PostGridFragment requires an item arguement!");
        }
        setupFragment();
        prepareBackgroundManager();
        setTag(item);
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.accent));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundRunnable != null) {
            mHandler.removeCallbacks(mBackgroundRunnable);
            mBackgroundRunnable = null;
        }
        mBackgroundManager = null;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsStopping = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mBackgroundManager.release();
        mIsStopping = true;
    }

    public boolean isStopping() {
        return mIsStopping;
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground =
                new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.bg_light_grey));
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    public void setTag(Object selectedItem) {
        String tag = null;
        if (selectedItem instanceof User) {
            mSelectedType = TYPE_USER;
            tag = ((User) selectedItem).userId;
            setTitle(((User) selectedItem).username);
        } else if (selectedItem instanceof Tag) {
            mSelectedType = TYPE_TAG;
            tag = ((Tag) selectedItem).tag;
            setTitle(String.format("#%s", tag));
        }
        mPostAdapter = new MovieAdapter(getActivity(), tag);
        setAdapter(mPostAdapter);
        addPageLoadSubscription();
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mHandler = new Handler();

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(SearchActivity.getStartIntent(getActivity()));
            }
        });

        setOnItemViewClickedListener(mOnItemViewClickedListener);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);
    }

    private void startBackgroundTimer(final URI backgroundURI) {
        if (mBackgroundRunnable != null) mHandler.removeCallbacks(mBackgroundRunnable);
        mBackgroundRunnable = new Runnable() {
            @Override
            public void run() {
                if (backgroundURI != null) updateBackground(backgroundURI.toString());
            }
        };
        mHandler.postDelayed(mBackgroundRunnable, BACKGROUND_UPDATE_DELAY);
    }

    protected void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .asBitmap()
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<Bitmap>(width, height) {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                GlideAnimation<? super Bitmap>
                                                        glideAnimation) {
                        mBackgroundManager.setBitmap(resource);
                    }
                });
        if (mBackgroundRunnable != null) mHandler.removeCallbacks(mBackgroundRunnable);
    }

    private void addPageLoadSubscription() {
        if (mPostAdapter.shouldShowLoadingIndicator()) mPostAdapter.showLoadingIndicator();

        Map<String, String> options = mPostAdapter.getAdapterOptions();
        String tag = options.get(PaginationAdapter.KEY_TAG);
        final String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        Observable<VineyardService.MovieResponse> observable = null;

        if (mSelectedType.equals(TYPE_TAG)) {
            observable = mDataManager.getVideosByTag(tag, nextPage, anchor);
        } else if (mSelectedType.equals(TYPE_USER)) {
            observable = mDataManager.getVideosByActor(tag, nextPage, anchor);
        }
        if (observable != null) {
            mCompositeSubscription.add(observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<VineyardService.MovieResponse>() {
                        @Override
                        public void onCompleted() { }

                        @Override
                        public void onError(Throwable e) {
                            mPostAdapter.removeLoadingIndicator();
                            if (mPostAdapter.size() == 0) {
                                mPostAdapter.showTryAgainCard();
                            } else {
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.error_message_loading_more_posts),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                            Timber.e("There was an error loading the posts", e);
                        }

                        @Override
                        public void onNext(VineyardService.MovieResponse movieResponse) {
                            mPostAdapter.removeLoadingIndicator();
                            if (mPostAdapter.size() == 0 && movieResponse.data.isEmpty()) {
                                mPostAdapter.showReloadCard();
                            } else {
                                if (anchor == null) {
//                                    mPostAdapter.setAnchor(movieResponse.data.anchorStr);
                                }
                                mPostAdapter.setNextPage(movieResponse.page.pageindex+1);
                                mPostAdapter.addAllItems(movieResponse.data);
                            }
                        }
                    }));
        }
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                if (NetworkUtil.isNetworkConnected(getActivity())) {
                    Movie post = (Movie) item;
                    ArrayList<Movie> postList = (ArrayList<Movie>) mPostAdapter.getAllItems();
                    startActivity(PlaybackActivity.newStartIntent(getActivity(), post, postList));
                } else {
                    ToastFactory.createWifiErrorToast(getActivity()).show();
                }
            } else if (item instanceof Option) {
                Option option = (Option) item;
                if (option.title.equals(getString(R.string.title_oops)) ||
                        option.title.equals(getString(R.string.title_no_videos))) {
                    mPostAdapter.removeReloadCard();
                    addPageLoadSubscription();
                }
            }
        }
    };

    private OnItemViewSelectedListener mOnItemViewSelectedListener = new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                String backgroundUrl = ((Movie) item).getBackgroundImageUrl();
                if (backgroundUrl != null) startBackgroundTimer(URI.create(backgroundUrl));
                ArrayList<Movie> posts = (ArrayList<Movie>) mPostAdapter.getAllItems();

                // If any item on the bottom row is selected...
                int itemIndex = mPostAdapter.indexOf(item);
                int minimumIndex = posts.size() - NUM_COLUMNS;
                if (itemIndex >= minimumIndex && mPostAdapter.shouldLoadNextPage()) {
                    addPageLoadSubscription();
                }
            }
        }
    };

}