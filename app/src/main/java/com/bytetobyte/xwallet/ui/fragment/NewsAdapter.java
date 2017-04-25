package com.bytetobyte.xwallet.ui.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.network.api.models.TwitterEntities;
import com.bytetobyte.xwallet.network.api.models.TwitterMedia;
import com.bytetobyte.xwallet.network.api.models.TwitterSearchResult;

import java.util.List;

/**
 * Created by bruno on 13.04.17.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final Context mContext;
    private List<TwitterSearchResult> mDataset;

    /**
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.news_row_text_body);
          //  mImageView = (ImageView) v.findViewById(R.id.news_row_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NewsAdapter(Context context, List<TwitterSearchResult> myDataset) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TwitterSearchResult searchResult = mDataset.get(position);
        holder.mTextView.setText(searchResult.getText());

        TwitterEntities entities = searchResult.getEntities();
        System.out.println("entities : " + entities);
        if (entities == null) return;

        TwitterMedia photo = entities.findPhoto();
        System.out.println("photo : " + photo);
        if (photo == null) return;

        //Resources res = mContext.getResources();
//        Glide.with(mContext)
//                .load(photo.getMediaUrl())
//                .asBitmap()
//                .fitCenter()
//                .into(holder.mImageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset == null)
            return 0;

        return mDataset.size();
    }
}
