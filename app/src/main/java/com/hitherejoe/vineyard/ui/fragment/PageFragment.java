/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hitherejoe.vineyard.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PageRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Category;
import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.DetailsActivity;
import com.hitherejoe.vineyard.ui.activity.PageActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.adapter.MovieAdapter;
import com.hitherejoe.vineyard.ui.adapter.PaginationAdapter;
import com.hitherejoe.vineyard.ui.presenter.CardPresenter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Sample {@link BrowseFragment} implementation showcasing the use of {@link PageRow} and
 * {@link ListRow}.
 */
public class PageFragment extends BrowseFragment {
    private static final long HEADER_ID_1 = 1;
    private static final String HEADER_NAME_1 = "Page Fragment";
    private static final long HEADER_ID_2 = 2;
    private static final String HEADER_NAME_2 = "Rows Fragment";
    private static final long HEADER_ID_3 = 3;
    private static final String HEADER_NAME_3 = "Settings Fragment";
    private static final long HEADER_ID_4 = 4;
    private static final String HEADER_NAME_4 = "User agreement Fragment";

    @Inject
    DataManager mDataManager;

    private BackgroundManager mBackgroundManager;

    private ArrayObjectAdapter mRowsAdapter;
    private HashMap<String, String> mCategoryMap;
    private int mCid;
    private Category mCategory;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataManager = VineyardApplication.get(getActivity()).getComponent().dataManager();

        Intent intent=getActivity().getIntent();

        mCid = intent.getIntExtra(PageActivity.CID, Category.MOVIE);

        mCategory = mDataManager.getCategoryById(mCid);


        setupUi();
        loadData();
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        getMainFragmentRegistry().registerFragment(CustomPageRow.class,
                new PageRowFragmentFactory(mBackgroundManager,mDataManager));
    }

    private void setupUi() {
        setTitle(mCategory.list_name);

        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
//        setBrandColor(getResources().getColor(R.color.fastlane_background));
//        setTitle("Title goes here");
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("cid",mCid);
                startActivity(intent);
//                Toast.makeText(
//                        getActivity(), getString(R.string.implement_search), Toast.LENGTH_SHORT)
//                        .show();
            }
        });



        prepareEntranceTransition();
    }

    private void loadData() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createRows();
                startEntranceTransition();
            }
        }, 2000);
    }

    private void createRows() {


        mCategoryMap=mCategory.getExtendValues();

        for (Map.Entry<String, String> entry: mCategoryMap.entrySet()){

            HeaderItem headerItem1 = new HeaderItem(HEADER_ID_1, entry.getKey());
            CustomPageRow pageRow1 = new CustomPageRow(headerItem1);

            pageRow1.options.put("cid",String.valueOf(mCid));
            pageRow1.options.put("achor",entry.getValue());

            mRowsAdapter.add(pageRow1);

        }

        //current



//        HeaderItem headerItem1 = new HeaderItem(HEADER_ID_1, HEADER_NAME_1);
//        PageRow pageRow1 = new PageRow(headerItem1);
//        mRowsAdapter.add(pageRow1);
//
//        HeaderItem headerItem2 = new HeaderItem(HEADER_ID_2, HEADER_NAME_2);
//        PageRow pageRow2 = new PageRow(headerItem2);
//        mRowsAdapter.add(pageRow2);
//
//        HeaderItem headerItem3 = new HeaderItem(HEADER_ID_3, HEADER_NAME_3);
//        PageRow pageRow3 = new PageRow(headerItem3);
//        mRowsAdapter.add(pageRow3);
//
//        HeaderItem headerItem4 = new HeaderItem(HEADER_ID_4, HEADER_NAME_4);
//        PageRow pageRow4 = new PageRow(headerItem4);
//        mRowsAdapter.add(pageRow4);
    }

    private static class CustomPageRow extends PageRow{
        public HashMap<String,String> options;

        public CustomPageRow(HeaderItem headerItem) {
            super(headerItem);
            options=new HashMap<>();
        }
    }

    private static class PageRowFragmentFactory extends BrowseFragment.FragmentFactory {
        private final BackgroundManager mBackgroundManager;

        private DataManager mDataManager;
        PageRowFragmentFactory(BackgroundManager backgroundManager,DataManager dataManager) {
            this.mBackgroundManager = backgroundManager;
            this.mDataManager=dataManager;
        }

        @Override
        public Fragment createFragment(Object rowObj) {
            Row row = (Row)rowObj;
            mBackgroundManager.setDrawable(null);
            if (row.getHeaderItem().getId() == HEADER_ID_1) {
                SampleFragmentA sampleFragmentA=null;
                if (row instanceof CustomPageRow){
                    HashMap<String,String> options=((CustomPageRow)row).options;

                    Bundle bundle=new Bundle();
                    for (Map.Entry<String, String> entry: options.entrySet()){

                        bundle.putString(entry.getKey(),entry.getValue());

                    }

                    sampleFragmentA= new SampleFragmentA();
                    sampleFragmentA.setArguments(bundle);
                    sampleFragmentA.setDataManager(mDataManager);

                }

                return sampleFragmentA;
            }
            else if (row.getHeaderItem().getId() == HEADER_ID_4) {
                return new WebViewFragment();
            }

            throw new IllegalArgumentException(String.format("Invalid row %s", rowObj));
        }
    }

    public static class PageFragmentAdapterImpl extends MainFragmentAdapter<SampleFragmentA> {

        public PageFragmentAdapterImpl(SampleFragmentA fragment) {
            super(fragment);
        }
    }

    /**
     * Simple page fragment implementation.
     */
    public static class SampleFragmentA extends GridFragment {
        private static final int COLUMNS = 6;
        private final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_SMALL;
        private MovieAdapter mAdapter;

        public void setDataManager(DataManager dataManager) {
            mDataManager = dataManager;

            if (mDataManager!=null){

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }, 500);
            }
        }

        private DataManager mDataManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }


        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);

            setupAdapter(args);
//            loadData();

        }


        private void setupAdapter(Bundle bundle) {
            VerticalGridPresenter presenter = new VerticalGridPresenter(ZOOM_FACTOR);
            presenter.setNumberOfColumns(COLUMNS);
            setGridPresenter(presenter);

            mAdapter = new MovieAdapter(getActivity(),"");
            mAdapter.setAnchor(bundle.getString("achor"));
            setAdapter(mAdapter);

            setOnItemViewClickedListener(new OnItemViewClickedListener() {
                @Override
                public void onItemClicked(
                        Presenter.ViewHolder itemViewHolder,
                        Object item,
                        RowPresenter.ViewHolder rowViewHolder,
                        Row row) {

                    Movie movie = (Movie) item;

                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra(DetailsActivity.MOVIE, movie);
                    getActivity().startActivity(intent);

//                    Card card = (Card)item;
//                    Toast.makeText(getActivity(),
//                            "Clicked on "+card.getTitle(),
//                            Toast.LENGTH_SHORT).show();
                }
            });

            setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
                @Override
                public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                    if (item instanceof Movie) {
                        String backgroundUrl = ((Movie) item).getBackgroundImageUrl();
//                        if (backgroundUrl != null) startBackgroundTimer(URI.create(backgroundUrl));


                        int index = mAdapter.indexOf(item);

                        Timber.d("index of item:"+index);

                        if (mAdapter.size()-index<=COLUMNS && mAdapter.size()>COLUMNS){
//                            mAdapter.showLoadingIndicator();

//                            if(mAdapter.shouldLoadNextPage()){
                                loadData();
//                            }
                        }

                    }
                }
            });

//            getMainFragmentAdapter().getFragmentHost().notifyViewCreated(getMainFragmentAdapter());
        }

        private void loadData() {

            Timber.d("loading data");

            Map<String, String> options = mAdapter.getAdapterOptions();
            String tag = options.get(PaginationAdapter.KEY_TAG);
            final String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
            String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);
            Observable<VineyardService.MovieResponse> observable=mDataManager.getMovies(nextPage,anchor);

            Subscription categorySubcription=observable
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
//                            mAdapter.removeLoadingIndicator();
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.error_message_retrieving_results),
                                    Toast.LENGTH_SHORT
                            ).show();
//                        Timber.e("There was an error loading the videos", e);
                        }

                        @Override
                        public void onNext(VineyardService.MovieResponse movieResponse) {

//                            mAdapter.removeLoadingIndicator();
//                            if (mAdapter.size() == 0 && (movieResponse.data==null || movieResponse.data.isEmpty())) {
//                                mAdapter.showReloadCard();
//                            }
//                            else {
                                mAdapter.setNextPage(movieResponse.page.pageindex + 1);

                                mAdapter.addAllItems(movieResponse.data);
//                            }


                        }
                    });

            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());

        }
    }


    public static class WebViewFragment extends Fragment implements MainFragmentAdapterProvider {
        private MainFragmentAdapter mMainFragmentAdapter = new MainFragmentAdapter(this);
        private WebView mWebview;

        @Override
        public MainFragmentAdapter getMainFragmentAdapter() {
            return mMainFragmentAdapter;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getMainFragmentAdapter().getFragmentHost().showTitleView(false);
        }

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FrameLayout root = new FrameLayout(getActivity());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMarginStart(32);
            mWebview = new WebView(getActivity());
            mWebview.setWebViewClient(new WebViewClient());
            mWebview.getSettings().setJavaScriptEnabled(true);
            root.addView(mWebview, lp);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            mWebview.loadUrl("https://www.google.com/policies/terms");
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
        }
    }
}
