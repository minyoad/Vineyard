package com.hitherejoe.vineyard.data;

import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Category;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Call;
import retrofit.Response;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

@Singleton
public class DataManager {

    private final VineyardService mVineyardService;
    private final PreferencesHelper mPreferencesHelper;

    private List<Category> mCategoryList;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, VineyardService vineyardService) {
        mPreferencesHelper = preferencesHelper;
        mVineyardService = vineyardService;

//        getCategoryList();
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Observable<Authentication> getAccessToken(String username, String password) {
        return mVineyardService.getAccessToken(username, password).map(new Func1<Authentication, Authentication>() {
            @Override
            public Authentication call(Authentication authentication) {
                if (authentication.success) {
                    mPreferencesHelper.putAccessToken(authentication.data.key);
                    mPreferencesHelper.putUsername(authentication.data.username);
                    mPreferencesHelper.putUserId(authentication.data.userId);
                }
                return authentication;
            }
        });
    }

    public Observable<User> getSignedInUser() {
        return mVineyardService.getSignedInUser();
    }

    public Observable<User> getUser(String userId) {
        return mVineyardService.getUser(userId);
    }

    public Observable<VineyardService.MovieResponse> getPopularPosts(String page, String anchor) {
        String wd = anchor != null ? "-cid-" + anchor : "";
        return mVineyardService.getPosts("plus-api-json-order-vod_hits-p-" + page + wd);
//        return mVineyardService.getPopularPosts(page, anchor);
    }

    public Observable<VineyardService.MovieResponse> getMovies(String page, String anchor) {
        return mVineyardService.getPosts("plus-api-json" + anchor + "-p-" + page);
    }

    public void getCategoryList() {

        if (mCategoryList!=null && mCategoryList.size()>0){
            Timber.d("already get categorylist,exit");
            return;
        }

        try {

            Response<VineyardService.CategoryListResponse> categoryListResponse = mVineyardService.getCategoryList().execute();

            VineyardService.CategoryListResponse categoryResponse = categoryListResponse.body();

            mCategoryList = new ArrayList<>();

            mCategoryList.addAll(categoryResponse.data);

//            handleRecommendations(movieResponse.data);
        } catch (IOException e) {
//            Timber.e("There was an error retrieving the posts", e);
            Timber.e("error when retrieving movies");
        }


    }

    public Category getCategoryById(String list_id) {
        if (mCategoryList != null)
            for (Category category : mCategoryList) {
                if (category.list_id.equals(list_id)) {
                    return category;
                }
            }

        return null;
    }

    public Call<VineyardService.MovieResponse> getPopularPostsSynchronous() {
        return mVineyardService.getPopularPosts();
    }

    public Observable<VineyardService.MovieResponse> getEditorsPicksPosts(String page, String anchor) {
        return mVineyardService.getEditorsPicksPosts(page, anchor);
    }

    public Observable<VineyardService.MovieResponse> getPostsByTag(String tag, String page, String anchor) {
        String wd = anchor != null ? "-cid-" + anchor : "";
        return mVineyardService.getPosts("plus-api-json-type-" + tag + "-p-" + page + wd);

    }

    public Observable<VineyardService.MovieResponse> getPostsByUser(String userId, String page, String anchor) {
        return mVineyardService.getUserTimeline(userId, page, anchor);
    }

    public Observable<VineyardService.TagResponse> searchByTag(String tag, String page, String anchor) {
        return mVineyardService.searchByTag(tag, page, anchor);

    }

    public Observable<VineyardService.UserResponse> searchByUser(String query, String page, String anchor) {
        return mVineyardService.searchByUser(query, page, anchor);
    }

    public Observable<VineyardService.KeywordSearchResponse> search(
            String tag, String pageOne, String anchorOne, String pageTwo, String anchorTwo) {
        return Observable.zip(searchByTag(tag, pageOne, anchorOne), searchByUser(tag, pageTwo, anchorTwo),
                new Func2<VineyardService.TagResponse, VineyardService.UserResponse, VineyardService.KeywordSearchResponse>() {
                    @Override
                    public VineyardService.KeywordSearchResponse call(VineyardService.TagResponse tagResponse, VineyardService.UserResponse userResponse) {
                        List<Tag> tags = tagResponse.data.records;
                        List<User> users = userResponse.data.records;

                        ArrayList<Object> results = new ArrayList<>();
                        results.addAll(tags);
                        results.addAll(users);

                        Collections.sort(results, new Comparator<Object>() {
                            @Override
                            public int compare(Object lhs, Object rhs) {
                                if (lhs instanceof Tag) {
                                    Tag tag = (Tag) lhs;
                                    if (rhs instanceof Tag) {
                                        Tag tagTwo = (Tag) rhs;
                                        return (int) (tag.postCount - tagTwo.postCount);
                                    } else if (rhs instanceof User) {
                                        User user = (User) rhs;
                                        return (int) (tag.postCount - user.followerCount);
                                    }
                                } else if (lhs instanceof User) {
                                    User user = (User) lhs;
                                    if (rhs instanceof Tag) {
                                        Tag tagTwo = (Tag) rhs;
                                        return (int) (user.followerCount - tagTwo.postCount);
                                    } else if (rhs instanceof User) {
                                        User userTwo = (User) rhs;
                                        return user.followerCount - userTwo.followerCount;
                                    }
                                }
                                return 0;
                            }
                        });

                        VineyardService.KeywordSearchResponse dualResponse = new VineyardService.KeywordSearchResponse();
                        dualResponse.tagSearchAnchor = tagResponse.data.anchorStr;
                        dualResponse.userSearchAnchor = userResponse.data.anchorStr;
                        dualResponse.list = results;

                        return dualResponse;
                    }
                });
    }
}
