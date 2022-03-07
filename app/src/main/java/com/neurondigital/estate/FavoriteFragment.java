package com.neurondigital.estate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.List;

import static com.neurondigital.estate.SinglePropertyActivity.ITEM_KEY;

/**
 * Created by melvin on 08/09/2016.
 *
 * Shows a list of the favorite Properties. The favorite properties are stored by id locally in preferences.
 * The content is however obtained from server.
 */
public class FavoriteFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    RelativeLayout empty;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();

        //set title
        getActivity().setTitle(getString(R.string.favorite_page_title));

        //set RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manage
        int spanCount = 1;
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
            mLayoutManager = new LinearLayoutManager(context);
        } else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            spanCount = ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Swipe to Refresh
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
    }

    /**
     * Refresh Favorite Properties.
     */
    public void refresh() {
        Property.getFavoriteProperties(getActivity(), new Property.onPropertiesDownloadedListener() {
            @Override
            public void onPropertiesDownloaded(List<Property> properties) {
                swipeLayout.setRefreshing(false);
                setProperties(properties);
            }
        });
    }


    /**
     * Show properties on screen after refresh
     * @param properties
     */
    public void setProperties(final List<Property> properties) {
        mAdapter = new PropertyAdapter(properties, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, SinglePropertyActivity.class);
                intent.putExtra(ITEM_KEY, properties.get(i).id);
                startActivity(intent);
            }
        }, context);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }

}
