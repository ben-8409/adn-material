package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

public class NewDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = NewDetailActivity.class.getSimpleName();
    private static final String BUNDLE_KEY_ITEM_ID = "item-id";
    private long mSelectedItemId;
    private Cursor mCursor;
    private TextView mAuthor;
    private TextView mText;
    private TextView mTitleTv;
    private NetworkImageView mToolbarImage;
    private String mTitle;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_detail);
        mToolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(NewDetailActivity.this)
                        .setType("text/plain")
                        .setText(mTitle)
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        //text view for content
        mText = findViewById(R.id.tv_content);
        mAuthor = findViewById(R.id.tv_author);
        mTitleTv = findViewById(R.id.tv_title);
        mToolbarImage = findViewById(R.id.appbar_image);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mSelectedItemId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        } else {
            mSelectedItemId = savedInstanceState.getLong(BUNDLE_KEY_ITEM_ID);
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_KEY_ITEM_ID, mSelectedItemId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newInstanceForItemId(this, mSelectedItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        hideLoadingIndicator();
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        bindViews();

    }

    private void bindViews() {
        if (mCursor != null) {
            mTitle = mCursor.getString(ArticleLoader.Query.TITLE);
            mAuthor.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));
            mTitleTv.setText(mTitle);
            mText.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
            mToolbarImage.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                    ImageLoaderHelper.getInstance(this).getImageLoader());
        }
    }

    private void hideLoadingIndicator() {
        View view = findViewById(R.id.loading_indicator);
        view.setVisibility(View.GONE);
    }
}
