package com.hitherejoe.vineyard.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import com.hitherejoe.vineyard.data.model.Category;
import com.hitherejoe.vineyard.data.model.Option;
import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.data.remote.VineyardService.MovieResponse;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.DetailsActivity;
import com.hitherejoe.vineyard.ui.activity.GuidedStepActivity;
import com.hitherejoe.vineyard.ui.activity.PageActivity;
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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
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

//    private String mPopularText;
//    private String mEditorsPicksText;
    private boolean mIsStopping;
    private HashMap<String, String> mCategoryMap;

    private Context mContext;

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

        mEventBus.register(this);

        downloadCategorySubcription();

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

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext=context;
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

//        setHeaderPresenterSelector(new PresenterSelector() {
//            @Override
//            public Presenter getPresenter(Object o) {
//                return new IconHeaderItemPresenter();
//            }
//        });


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

        Category category=mDataManager.getCategoryById(Category.INDEX);

        if(category==null){
            return;
        }
        mCategoryMap = category.getExtendValues();

        for (final Map.Entry<String, String> entry: mCategoryMap.entrySet()){

            Activity activity=getActivity();
            if(activity==null)
                return;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadPostsFromCategory(entry.getKey(),0);
                    startEntranceTransition();
                }

        });

        }

//        boolean shouldAutoLoop = mPreferencesHelper.getShouldAutoLoop();
//        String optionValue = shouldAutoLoop
//                ? getString(R.string.text_auto_loop_enabled)
//                : getString(R.string.text_auto_loop_disabled);
//
//        mAutoLoopOption = new Option(
//                getString(R.string.text_auto_loop_title),
//                optionValue,
//                R.drawable.lopp);
//
//
//        HeaderItem gridHeader =
//                new HeaderItem(mRowsAdapter.size(), getString(R.string.header_text_options));
//        mOptionsAdapter = new OptionsAdapter(getActivity());
//        mOptionsAdapter.addOption(mAutoLoopOption);
//        mRowsAdapter.add(new ListRow(gridHeader, mOptionsAdapter));

    }

    private void loadPostsFromCategory(String tag, int headerPosition) {
        MovieAdapter listRowAdapter = new MovieAdapter(getActivity(), String.valueOf(headerPosition));

        listRowAdapter.setAnchor(mCategoryMap.get(tag));

        addPostLoadSubscription(listRowAdapter);
        HeaderItem header = new HeaderItem(headerPosition, tag);
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        try {
            mBackgroundManager.attach(getActivity().getWindow());
        }catch (Exception e){

        }
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

        Observable<MovieResponse> observable=mDataManager.getMovies(nextPage,anchor,10);

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
                        Timber.e("There was an error loading the posts");
                    }

                    @Override
                    public void onNext(MovieResponse movieResponse) {

                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0 && (movieResponse.data==null || movieResponse.data.isEmpty())) {
                            adapter.showReloadCard();
                        } else {
//                            if (anchor == null) adapter.setAnchor(movieResponse.data.anchorStr);
//                            adapter.setNextPage(movieResponse.page.pageindex+1);
                            adapter.addAllItems(movieResponse.data);

                            adapter.showLoadMoreCard();
                            adapter.setNextPage(0);
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
                    Movie movie = (Movie) item;

                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra(DetailsActivity.MOVIE, movie);
                    getActivity().startActivity(intent);

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

                    Intent intent=new Intent(getActivity().getBaseContext(), PageActivity.class);

                    int index = mRowsAdapter.indexOf(row);

                    String header=((ListRow) mRowsAdapter.get(index)).getHeaderItem().getName();
                    String achor=mCategoryMap.get(header);

                    int cid=Category.idFromAchor(achor);

                    intent.putExtra(PageActivity.ACHOR,mCategoryMap.get(header));
                    intent.putExtra(PageActivity.CURRENT,header);
                    intent.putExtra(PageActivity.CID,cid);

                    startActivity(intent);

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

    public void downloadCategorySubcription(){

        Observable<VineyardService.CategoryListResponse> observable=mDataManager.downloadCategoryList();

        Subscription categorySubcription=observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.CategoryListResponse>() {
                    @Override
                    public void onCompleted() {

                        loadPosts();

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
                    public void onNext(VineyardService.CategoryListResponse categoryListResponse) {

                        mDataManager.getCategoryList().addAll(categoryListResponse.data);

                    }
                });
    }

}