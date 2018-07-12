package com.hitherejoe.vineyard.data;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.local.PlayerHelper;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Category;
import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Call;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

@Singleton
public class DataManager {

    private final VineyardService mVineyardService;
    private final PreferencesHelper mPreferencesHelper;

    public List<Category> getCategoryList() {
        return mCategoryList;
    }

    private List<Category> mCategoryList;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, VineyardService vineyardService) {
        mPreferencesHelper = preferencesHelper;
        mVineyardService = vineyardService;

        mCategoryList=new LinkedList<>();

    }

//    public PlayerHelper getPlayerHelper(){
//        return new PlayerHelper();
//
//    }

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

    public Observable<VineyardService.MovieResponse> getPopularMovies(String page, String anchor) {
        String wd = anchor != null ? "-cid-" + anchor : "";
        return mVineyardService.getMovies("plus-api-json-order-vod_hits-p-" + page + wd);
    }

    public Observable<VineyardService.MovieResponse> getMovies(String page, String anchor) {
        return mVineyardService.getMovies("plus-api-json" + anchor + "-p-" + page);
    }

    public Observable<VineyardService.MovieResponse> getMovies(String page, String anchor,int limit) {
        return mVineyardService.getMovies("plus-api-json" + anchor + "-p-" + page+"-limit-"+limit);
    }

    public Observable<VineyardService.MovieResponse>getRelatedMovies(Movie movie){
        return getMovies("1","-star-"+movie.vod_actor+"-limit-6");
    }

    public Observable<VineyardService.CategoryListResponse> downloadCategoryList(){
        return mVineyardService.getCategoryList();
    }


    public Category getCategoryById(int list_id) {
        if (mCategoryList != null) {
            for (Category category : mCategoryList) {
                if (category.list_id.equals(String.valueOf(list_id))) {
                    return category;
                }
            }
        }

        return null;
    }

    public Call<VineyardService.MovieResponse> getPopularMoviesSynchronous() {
        return mVineyardService.getPopularMovies();
    }

    public Call<VineyardService.PlayerResponse> getPlayerList(){
        return mVineyardService.getPlayerList();
    }

    public Observable<VineyardService.MovieResponse> getEditorsPicksMovies(String page, String anchor) {
        return mVineyardService.getEditorsPicksMovies(page, anchor);
    }

    public Observable<VineyardService.MovieResponse> getVideosByTag(String tag, String page, String anchor) {
        String wd = anchor != null ? "-cid-" + anchor : "";
        return mVineyardService.getMovies("plus-api-json-type-" + tag + "-p-" + page + wd);

    }

    public Observable<VineyardService.MovieResponse> getVideosByActor(String actor, String page, String cid) {
        return getMovies(page,"-wd-"+actor+"-cid-"+cid);
//        return mVineyardService.getUserTimeline(actor, page, cid);
    }

    public Observable<VineyardService.TagResponse> searchByTag(String tag, String page, String anchor) {
        return mVineyardService.searchByTag(tag, page, anchor);

    }

    public Observable<VineyardService.UserResponse> searchByUser(String query, String page, String anchor) {
        return mVineyardService.searchByUser(query, page, anchor);
    }

    public Observable<VineyardService.MovieResponse> search(String page,String keyword){
        return getMovies(page,"-wd-"+keyword,12);
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
