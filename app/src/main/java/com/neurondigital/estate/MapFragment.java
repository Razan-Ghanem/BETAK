package com.neurondigital.estate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.gc.materialdesign.views.ButtonFlat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.neurondigital.estate.SinglePropertyActivity.ITEM_KEY;

/**
 * Created by melvin on 08/09/2016.
 * Shows a full screen map with the property list in the bottom.
 */
public class MapFragment extends Fragment {
    Context context;

    //list
    private EmptyRecyclerView mRecyclerView;
    private EmptyRecyclerView.Adapter mAdapter;
    private EmptyRecyclerView.LayoutManager mLayoutManager;

    //filter button and search
    EditText searchView;
    ButtonFlat filterBtn;

    //map
    private GoogleMap map;
    List<Marker> markers = new ArrayList<Marker>();
    RecyclerViewPositionHelper posHelper;

    //all properties
    Map<String, String> filterParams = new HashMap<String, String>();
    EndlessRecyclerViewScrollListener scrollListener;
    List<Property> properties;
    Intent filterIntentData;
    BitmapDescriptor pin;
    double targetLat, targetLng;

    public final static int FILTER_REQUEST_CODE = 1547;

    public final static int LIST_INITIAL_LOAD = 10;
    public final static int LIST_INITIAL_LOAD_MORE_ONSCROLL = 5;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.propertyList);
        RelativeLayout empty = (RelativeLayout) rootView.findViewById(R.id.empty);
        mRecyclerView.setEmptyView(empty);
        searchView = (EditText) rootView.findViewById(R.id.searchView);
        filterBtn = (ButtonFlat) rootView.findViewById(R.id.filterBtn);

        //set up map when it is loaded
        if (map == null) {
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            int i = (int) marker.getTag();
                            Intent intent = new Intent(context, SinglePropertyActivity.class);
                            intent.putExtra(ITEM_KEY, properties.get(i).id);
                            startActivity(intent);
                            return false;
                        }
                    });

                }


            });
        }
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();
        getActivity().setTitle(getString(R.string.search_title));

        //load pin image
        pin = BitmapDescriptorFactory.fromResource(R.drawable.pin);

        //filter btn
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
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        posHelper = new RecyclerViewPositionHelper(mRecyclerView);

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
                if (properties.size() > 0) {
                    Property p = properties.get(posHelper.findFirstVisibleItemPosition());
                    gotoMap(p.gpslat, p.gpslng);
                }
            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);

        refresh();
    }

    /**
     * Refresh property list from server
     */
    public void refresh() {

        Property.loadProperties(getActivity(), 0, LIST_INITIAL_LOAD, filterParams, new Property.onPropertiesDownloadedListener() {
            @Override
            public void onPropertiesDownloaded(List<Property> properties) {
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
                ((MapPropertyAdapter) mAdapter).addItems(properties);
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

        //clear markers
        markers.clear();
        map.clear();

        //add new markers
        for (int i = 0; i < properties.size(); i++) {
            LatLng coordinate = new LatLng(properties.get(i).gpslat, properties.get(i).gpslng);
            Marker marker = map.addMarker(new MarkerOptions().position(coordinate).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin", 100))));
            marker.setTag(i);
            markers.add(marker);
        }

        //create list
        mAdapter = new MapPropertyAdapter(properties, new AdapterView.OnItemClickListener() {
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

        //set marker to first item
        if (properties.size() > 0) {
            Property p = properties.get(0);
            gotoMap(p.gpslat, p.gpslng);
        }

    }

    /**
     * Resize map icon so that it is responsive
     * @param iconName
     * @param width
     * @return
     */
    public Bitmap resizeMapIcons(String iconName, int width) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", context.getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, (int) (((float) width / (float) imageBitmap.getWidth()) * imageBitmap.getHeight()), false);
    }


    /**
     * Animate map camera position to new lat/lng
     *
     * @param lat
     * @param lng
     */
    public void gotoMap(double lat, double lng) {
        if (map == null)
            return;

        //do not animate if already there
        if (!(lat == targetLat && lng == targetLng)) {
            targetLat = lat;
            targetLng = lng;

            //convert to LatLng
            LatLng coordinate = new LatLng(lat, lng);

            // Save current zoom
            float originalZoom = map.getCameraPosition().zoom;

            // Move temporarily camera zoom
            map.moveCamera(CameraUpdateFactory.zoomTo(13));

            //get display height
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            //offset
            Point pointInScreen = map.getProjection().toScreenLocation(coordinate);
            Point newPoint = new Point();
            newPoint.x = pointInScreen.x;
            newPoint.y = pointInScreen.y + (int) (size.y / 4.5f);
            LatLng newCenterLatLng = map.getProjection().fromScreenLocation(newPoint);

            // Restore original zoom
            map.moveCamera(CameraUpdateFactory.zoomTo(originalZoom));

            //animate
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, 13));
        }
    }

    /**
     * Ask user to rate
     */
    public boolean AskRate() {
        return Rate.rateWithCounter(getActivity(), getResources().getInteger(R.integer.rate_shows_after_X_starts), getResources().getString(R.string.rate_title), getResources().getString(R.string.rate_text), getResources().getString(R.string.unable_to_reach_market), getResources().getString(R.string.Alert_accept), getResources().getString(R.string.Alert_cancel));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //filter screen returned data
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
        inflater.inflate(R.menu.main_menu_map, menu);

        //set list icon from FontAwsome
        menu.findItem(R.id.list).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_list)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle share
        switch (item.getItemId()) {
            case R.id.list:
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.mainFragment, new SearchFragment(), "");
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
