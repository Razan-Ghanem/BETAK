package com.neurondigital.estate;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by melvin on 08/09/2016.
 * Agent Fragment, which shows a list of all agents
 */
public class AgentsFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    EndlessRecyclerViewScrollListener scrollListener;
    List<Agent> agents;

    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agents, container, false);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        RelativeLayout empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        getActivity().setTitle(getString(R.string.agents_title));


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        int spanCount = 1;
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //handle when user scrolls more than the items on screen
        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager, spanCount) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                System.out.println("load more" + totalItemsCount);
                loadMore(totalItemsCount);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {

            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refreshes the WebView
                refresh();
            }
        });

        refresh();


    }


    /**
     * Refresh agent list from server
     */
    public void refresh() {
        Agent.loadAgents(getActivity(), 0, LIST_INITIAL_LOAD, "", new Agent.onAgentsDownloadedListener() {
            @Override
            public void onAgentsDownloaded(List<Agent> agent) {
                swipeLayout.setRefreshing(false);
                setAgents(agent);
            }
        });
    }


    /**
     * Load more agents from server
     *
     * @param first - start loading from this agent
     */
    public void loadMore(int first) {
        Agent.loadAgents(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, "", new Agent.onAgentsDownloadedListener() {
            @Override
            public void onAgentsDownloaded(List<Agent> agents) {
                swipeLayout.setRefreshing(false);
                ((AgentAdapter) mAdapter).addItems(agents);
                mRecyclerView.swapAdapter(mAdapter, false);
            }
        });
    }


    /**
     * Set the agents to show on screen
     * @param agents_loaded
     */
    public void setAgents(final List<Agent> agents_loaded) {
        this.agents = agents_loaded;
        mAdapter = new AgentAdapter(agents, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }, context);
        mRecyclerView.swapAdapter(mAdapter, false);
        scrollListener.resetState();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


}
