package com.hitherejoe.vineyard.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;

import com.hitherejoe.vineyard.R;

/**
 * {@link VerticalGridActivity} loads {@link VerticalGridFragment}
 */
public class VerticalGridActivity extends Activity {

    private static final String TAG = VerticalGridActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_grid);
    }
}
