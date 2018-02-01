package com.hitherejoe.vineyard.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.BusEvent;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Option;
import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.data.remote.VineyardService.MovieResponse;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.GuidedStepActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.adapter.OptionsAdapter;
import com.hitherejoe.vineyard.ui.adapter.PaginationAdapter;
import com.hitherejoe.vineyard.ui.adapter.MovieAdapter;
import com.hitherejoe.vineyard.ui.presenter.IconHeaderItemPresenter;
import com.hitherejoe.vineyard.util.NetworkUtil;
import com.hitherejoe.vineyard.util.ToastFactory;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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

public class MainFragment extends BrowseFragment {

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    @Inject Bus mEventBus;
    @Inject CompositeSubscription mCompositeSubscription;
    @Inject DataManager mDataManager;
    private PreferencesHelper mPreferencesHelper;

    private ArrayObjectAdapter mRowsAdapter;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private Handler mHandler;
    private Option mAutoLoopOption;
    private OptionsAdapter mOptionsAdapter;
    private Runnable mBackgroundRunnable;

    private String mPopularText;
    private String mEditorsPicksText;
    private boolean mIsStopping;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        mPreferencesHelper =
                VineyardApplication.get(getActivity()).getComponent().preferencesHelper();
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mHandler = new Handler();
        mPopularText = getString(R.string.header_text_popular);
        mEditorsPicksText = getString(R.string.header_text_editors_picks);
        mEventBus.register(this);

        loadPosts();
        setAdapter(mRowsAdapter);
        prepareBackgroundManager();
        setupUIElements();
        setupListeners();
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
        mEventBus.unregister(this);
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

    @Subscribe
    public void onAutoLoopUpdated(BusEvent.AutoLoopUpdated event) {
        boolean isEnabled = mPreferencesHelper.getShouldAutoLoop();
        mAutoLoopOption.value = isEnabled
                ? getString(R.string.text_auto_loop_enabled)
                : getString(R.string.text_auto_loop_disabled);
        mOptionsAdapter.updateOption(mAutoLoopOption);
    }

    public boolean isStopping() {
        return mIsStopping;
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

    private void setupUIElements() {
        setBadgeDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.banner_shadow));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary));
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.accent));

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });

        boolean shouldAutoLoop = mPreferencesHelper.getShouldAutoLoop();
        String optionValue = shouldAutoLoop
                ? getString(R.string.text_auto_loop_enabled)
                : getString(R.string.text_auto_loop_disabled);

        mAutoLoopOption = new Option(
                getString(R.string.text_auto_loop_title),
                optionValue,
                R.drawable.lopp);


        HeaderItem gridHeader =
                new HeaderItem(mRowsAdapter.size(), getString(R.string.header_text_options));
        mOptionsAdapter = new OptionsAdapter(getActivity());
        mOptionsAdapter.addOption(mAutoLoopOption);
        mRowsAdapter.add(new ListRow(gridHeader, mOptionsAdapter));
    }

    private void setupListeners() {
        setOnItemViewClickedListener(mOnItemViewClickedListener);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);

        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadPosts() {
        String[] categories = getResources().getStringArray(R.array.categories);
        for (int i = 0; i < categories.length; i++) loadPostsFromCategory(categories[i], i);
    }

    private void loadPostsFromCategory(String tag, int headerPosition) {
        MovieAdapter listRowAdapter = new MovieAdapter(getActivity(), tag);
        addPostLoadSubscription(listRowAdapter);
        HeaderItem header = new HeaderItem(headerPosition, tag);
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground =
                new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.bg_grey));
        mBackgroundManager.setColor(ContextCompat.getColor(getActivity(), R.color.bg_grey));
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
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

    private void addPostLoadSubscription(final MovieAdapter adapter) {
        if (adapter.shouldShowLoadingIndicator()) adapter.showLoadingIndicator();

        Map<String, String> options = adapter.getAdapterOptions();
        String tag = options.get(PaginationAdapter.KEY_TAG);
        final String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        Observable<MovieResponse> observable;
        if (tag.equals(mPopularText)) {
            observable = mDataManager.getPopularPosts(nextPage, anchor);
        } else if (tag.equals(mEditorsPicksText)) {
            observable = mDataManager.getEditorsPicksPosts(nextPage, anchor);
        } else {
            observable = mDataManager.getPostsByTag(tag, nextPage, anchor);
        }

        mCompositeSubscription.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<MovieResponse>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0) {
                            adapter.showTryAgainCard();
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
                    public void onNext(MovieResponse movieResponse) {
                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0 && (movieResponse.data==null || movieResponse.data.isEmpty())) {
                            adapter.showReloadCard();
                        } else {
//                            if (anchor == null) adapter.setAnchor(movieResponse.data.anchorStr);
                            adapter.setNextPage(movieResponse.page.pageindex+1);
                            adapter.addAllItems(movieResponse.data);
                        }
                    }
                }));
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                if (NetworkUtil.isNetworkConnected(getActivity())) {
                    Movie post = (Movie) item;
                    int index = mRowsAdapter.indexOf(row);
                    MovieAdapter adapter =
                            ((MovieAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                    ArrayList<Movie> postList = (ArrayList<Movie>) adapter.getAllItems();
                    startActivity(PlaybackActivity.newStartIntent(getActivity(), post, postList));
                } else {
                    ToastFactory.createWifiErrorToast(getActivity()).show();
                }
            } else if (item instanceof Option) {
                Option option = (Option) item;
                if (option.title.equals(getString(R.string.title_no_videos)) ||
                        option.title.equals(getString(R.string.title_oops))) {
                    int index = mRowsAdapter.indexOf(row);
                    MovieAdapter adapter =
                            ((MovieAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                    adapter.removeReloadCard();
                    addPostLoadSubscription(adapter);
                } else {
                    startActivity(GuidedStepActivity.getStartIntent(getActivity()));
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
                int index = mRowsAdapter.indexOf(row);
                MovieAdapter adapter =
                        ((MovieAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                if (adapter.get(adapter.size() - 1).equals(item) && adapter.shouldLoadNextPage()) {
                    addPostLoadSubscription(adapter);
                }
            }
        }
    };

}