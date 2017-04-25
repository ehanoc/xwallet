package com.bytetobyte.xwallet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytetobyte.xwallet.BaseFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.network.api.TwitterSearchApi;
import com.bytetobyte.xwallet.network.api.models.TwitterSearchResult;
import com.bytetobyte.xwallet.network.api.models.TwitterSearchStatuses;

import java.util.List;

/**
 * Created by bruno on 08.04.17.
 */
public class NewsFragment extends BaseFragment implements TwitterSearchApi.TwitterSearchCallback {

    private RecyclerView _recyclerView;
    private RecyclerView.Adapter _newsAdapter;
    private LinearLayoutManager _layoutManager;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);
        _recyclerView = (RecyclerView) rootView.findViewById(R.id.news_recycler_view);
        onSearchResult(new TwitterSearchStatuses());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        String token = getBaseActivity().getNewsAuthToken();
        new TwitterSearchApi("from:BTCTN", token, this).execute();
    }

    /**
     *
     * @param result
     */
    @Override
    public void onSearchResult(TwitterSearchStatuses result) {
        System.out.println("Twitter News result : " + result);

        List<TwitterSearchResult> statuses = result.getStatuses();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        _layoutManager = new LinearLayoutManager(getBaseActivity());
        _recyclerView.setLayoutManager(_layoutManager);

        _recyclerView.addItemDecoration(new RecyclerViewItemDecorator(5, 1));

        _newsAdapter = new NewsAdapter(getBaseActivity(), statuses);
        _recyclerView.setAdapter(_newsAdapter);

        _newsAdapter.notifyDataSetChanged();
    }
}
