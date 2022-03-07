package com.neurondigital.estate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.gc.materialdesign.views.ButtonFlat;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.neurondigital.estate.SinglePropertyActivity.ITEM_KEY;

/**
 * Created by melvin on 08/09/2016.
 * A fragment that shows a list of properties and provides features for the user to search and filter properties.
 */
public class SearchFragment extends Fragment {
    Context context;
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout swipeLayout;
    EditText searchView;
    ButtonFlat filterBtn;
    Map<String, String> filterParams = new HashMap<String, String>();
    EndlessRecyclerViewScrollListener scrollListener;
    List<Property> properties;
    Intent filterIntentData;

    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;

    public final static int FILTER_REQUEST_CODE = 1547;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        RelativeLayout empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        searchView = (EditText) rootView.findViewById(R.id.searchView);
        filterBtn = (ButtonFlat) rootView.findViewById(R.id.filterBtn);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();
        getActivity().setTitle(getString(R.string.search_title));

        //filter button
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FilterActivity.class);
                if (filterIntentData != null)
                    intent.putExtras(filterIntentData);
                startActivityForResult(intent, FILTER_REQUEST_CODE);
            }
        });

        //search bar
        Functions.tintColorWidget(searchView, R.color.colorWhite, context);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                System.out.println("load more search" + searchView.getText().toString());
                filterParams.put("search", searchView.getText().toString());
                refresh();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
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

        //filterParams = new HashMap<String, String>();
        filterParams.put("search", searchView.getText().toString());
        refresh();

        //close keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    /**
     * Refresh property list from server
     */
    public void refresh() {

        Property.loadProperties(getActivity(), 0, LIST_INITIAL_LOAD, filterParams, new Property.onPropertiesDownloadedListener() {
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
        Property.loadProperties(getActivity(), first, LIST_INITIAL_LOAD_MORE_ONSCROLL, filterParams, new Property.onPropertiesDownloadedListener() {
            @Override
            public void onPropertiesDownloaded(List<Property> properties) {
                swipeLayout.setRefreshing(false);
                ((PropertyAdapter) mAdapter).addItems(properties);
                mRecyclerView.swapAdapter(mAdapter, false);
            }
        });
    }


    /**
     * Set property list
     * @param properties_loaded
     */
    public void setProperties(final List<Property> properties_loaded) {
        this.properties = properties_loaded;
        mAdapter = new PropertyAdapter(properties, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //open ad. If ad not open attempt to open rate
                if (!((MainActivity) getActivity()).loadInterstitial()) {
                    if (!AskRate()) {
                        System.out.println("click: " + properties.get(i).id + "  " + properties.get(i).name);
                        Intent intent = new Intent(context, SinglePropertyActivity.class);
                        intent.putExtra(ITEM_KEY, properties.get(i).id);
                        startActivity(intent);
                    }
                }
            }
        }, context);
        mRecyclerView.swapAdapter(mAdapter, false);
        scrollListener.resetState();

    }


    /**
     * Ask user to rate
     */
    public boolean AskRate() {
        return Rate.rateWithCounter(getActivity(), getResources().getInteger(R.integer.rate_shows_after_X_starts), getResources().getString(R.string.rate_title), getResources().getString(R.string.rate_text), getResources().getString(R.string.unable_to_reach_market), getResources().getString(R.string.Alert_accept), getResources().getString(R.string.Alert_cancel));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                filterIntentData = data;

                //get all the options returned by the filter screen
                int status = data.getIntExtra(FilterActivity.KEY_STATUS, 0);
                int type = data.getIntExtra(FilterActivity.KEY_TYPE, 0);
                int maximum_price = data.getIntExtra(FilterActivity.KEY_MAX_PRICE, 0);
                int minimum_price = data.getIntExtra(FilterActivity.KEY_MIN_PRICE, 0);
                int minimum_bedrooms = data.getIntExtra(FilterActivity.KEY_BEDS, 0);
                int minimum_bathrooms = data.getIntExtra(FilterActivity.KEY_BATHS, 0);
                int minimum_rooms = data.getIntExtra(FilterActivity.KEY_ROOMS, 0);

                //set hashmap which can be sen to server
                filterParams = new HashMap<String, String>();
                filterParams.put("search", searchView.getText().toString());
                if (status > 0)
                    filterParams.put("status", "" + status);
                if (type > 0)
                    filterParams.put("type", "" + type);
                if (minimum_price > 0)
                    filterParams.put("minimum_saleprice", "" + minimum_price);
                if (maximum_price > 0)
                    filterParams.put("maximum_saleprice", "" + maximum_price);
                if (minimum_bedrooms > 0)
                    filterParams.put("minimum_bedrooms", "" + minimum_bedrooms);
                if (minimum_bathrooms > 0)
                    filterParams.put("minimum_bathrooms", "" + minimum_bathrooms);
                if (minimum_rooms > 0)
                    filterParams.put("minimum_rooms", "" + minimum_rooms);

                //refresh list
                refresh();
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main_menu_list, menu);

        //set map icon from FontAwsome
        menu.findItem(R.id.map).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_map)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle switch to map
        switch (item.getItemId()) {
            case R.id.map:
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.mainFragment, new MapFragment(), "");
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
