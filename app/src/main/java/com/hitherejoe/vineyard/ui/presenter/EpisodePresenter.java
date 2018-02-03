package com.hitherejoe.vineyard.ui.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.ViewGroup;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Movie;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.ui.widget.EpisodeCardView;
import com.hitherejoe.vineyard.ui.widget.TagCardView;

public class EpisodePresenter extends Presenter {

    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        sDefaultBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.primary);
        sSelectedBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.primary_dark);

        EpisodeCardView cardView = new EpisodeCardView(parent.getContext());


        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(EpisodeCardView view, boolean selected) {
        view.setBackgroundColor(selected ? sSelectedBackgroundColor : sDefaultBackgroundColor);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof Movie.PlayUrlInfo) {
            Movie.PlayUrlInfo post = (Movie.PlayUrlInfo) item;
            EpisodeCardView cardView = (EpisodeCardView) viewHolder.view;

            if (post.title != null) {
                cardView.setCardText(post.title);
                cardView.setCardIcon(R.drawable.ic_tag);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }

}