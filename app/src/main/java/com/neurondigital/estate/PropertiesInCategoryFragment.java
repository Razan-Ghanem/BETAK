package com.neurondigital.estate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import static com.neurondigital.estate.SinglePropertyActivity.ITEM_KEY;

/**
 * Created by melvin on 08/09/2016.
 * Shows a list of properties that belong to a category
 */
public class PropertiesInCategoryFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    int categoryId = 0;

    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
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

        //set RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        int spanCount = 1;
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
            mLayoutManager = new LinearLayoutManager(context);

        } else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            spanCount = ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        //handle when user scrolls more than the items on screen
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager, spanCount) {
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


        //get category Id from
        categoryId = getArguments().getInt("Category_id", 0);

        //load category from server
        Category.getCategoryName(context, categoryId, new Category.onNameFoundListener() {
            @Override
            public void onNameFound(String name) {
                getActivity().setTitle(name);
            }
        });

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
     * Refresh property list from server
     */
    public void refresh() {
        Property.loadProperties(getActivity(), 0, LIST_INITIAL_LOAD, "", "" + categoryId, new Property.onPropertiesDownloadedListener() {
            @Override
            public void onPropertiesDownloaded(List<Property> properties) {
                swipeLayout.setRefreshing(false);
                setProperties(properties);
            }
        });
    }

    /**
     * Load more properties from server
     *
     * @param first - start loading from this property
     */
    public void loadMore(int first) {
        Property.loadProperties(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, "", "" + categoryId, new Property.onPropertiesDownloadedListener() {
            @Override
            public void onPropertiesDownloaded(List<Property> properties) {
                swipeLayout.setRefreshing(false);
                ((PropertyAdapter) mAdapter).addItems(properties);
            }
        });
    }


    /**
     * Show properties to screen
     *
     * @param properties - list of properties to show
     */
    public void setProperties(final List<Property> properties) {
        mAdapter = new PropertyAdapter(properties, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //open property in new activity on click
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
        //clear options menu
        menu.clear();

        //re-initialise menu
        inflater.inflate(R.menu.options_menu, menu);

        //set search icon using FontAwesome
        menu.findItem(R.id.search).setIcon(
                new IconicsDrawable(getContext())
                        .icon(FontAwesome.Icon.faw_search)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));

        //set search feature
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = new SearchView(((MainActivity) context).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Search for properties on server when text changed
                Property.loadProperties(getActivity(), 0, 1000, newText, "" + categoryId, new Property.onPropertiesDownloadedListener() {
                    @Override
                    public void onPropertiesDownloaded(List<Property> properties) {
                        setProperties(properties);
                    }
                });
                return false;
            }
        });
    }
}
