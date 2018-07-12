package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;

import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.ui.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieAdapter extends PaginationAdapter {

    public MovieAdapter(Context context, String tag) {
        super(context, new CardPresenter(context), tag);
    }

    @Override
    public void addAllItems(List<?> items) {
        List<Movie> currentMovies = getAllItems();
        ArrayList<Movie> posts = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Object object = items.get(i);
            if (object instanceof Movie && !currentMovies.contains(object)) {
                posts.add((Movie) object);
            }
        }
        Collections.sort(posts);
        addMovies(posts);
    }

    @Override
    public List<Movie> getAllItems() {
        List<Object> itemList = getItems();
        ArrayList<Movie> posts = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            Object object = itemList.get(i);
            if (object instanceof Movie) posts.add((Movie) object);
        }
        return posts;
    }
}
