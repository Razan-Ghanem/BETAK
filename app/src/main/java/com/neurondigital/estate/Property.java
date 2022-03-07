package com.neurondigital.estate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neurondigital.estate.Functions.urlEncodeUTF8;
import static com.neurondigital.estate.SinglePropertyActivity.ITEM_KEY;

/**
 * Created by melvin on 25/09/2016.
 */
public class Property {
    public int id;
    public String name, area;
    public String description, address, county, city, zipcode, yearbuilt;
    public int bathrooms, bedrooms, rentprice, saleprice, status, type, energy, rooms;
    public double gpslat, gpslng;
    public String imageUrl[];
    public int viewed, shared, favorited;
    public String typeName, statusName, energyName;
    public String ownerName, tel, email;


    interface onPropertyDownloadedListener {
        void onPropertyDownloaded(Property property);
    }

    interface onPropertiesDownloadedListener {
        void onPropertiesDownloaded(List<Property> properties);
    }

    public Property(int id, String name, String[] imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Property(String name) {
        this.name = name;
    }


    /**
     * Decode property from JSON
     *
     * @param JSONProperty
     */
    public Property(JSONObject JSONProperty) {
        try {
            id = JSONProperty.getInt("id");
            name = JSONProperty.getString("name");
            description = JSONProperty.getString("description");
            address = JSONProperty.getString("address");
            county = JSONProperty.getString("county");
            zipcode = JSONProperty.getString("zipcode");
            city = JSONProperty.getString("city");
            yearbuilt = JSONProperty.getString("yearbuilt");
            area = JSONProperty.getString("area");
            bathrooms = JSONProperty.getInt("bathrooms");
            bedrooms = JSONProperty.getInt("bedrooms");
            rentprice = JSONProperty.getInt("rentprice");
            saleprice = JSONProperty.getInt("saleprice");
            status = JSONProperty.getInt("status");
            type = JSONProperty.getInt("type");
            energy = JSONProperty.getInt("energy");
            rooms = JSONProperty.getInt("rooms");
            viewed = JSONProperty.getInt("viewed");
            shared = JSONProperty.getInt("shared");
            favorited = JSONProperty.getInt("favorited");
            gpslat = JSONProperty.getDouble("gpslat");
            gpslng = JSONProperty.getDouble("gpslng");
            ownerName = JSONProperty.getString("ownername");
            tel = JSONProperty.getString("tel");
            email = JSONProperty.getString("email");

            JSONArray JSONimageURL = new JSONArray(JSONProperty.getString("image"));
            imageUrl = new String[JSONimageURL.length()];
            for (int i = 0; i < JSONimageURL.length(); i++) {
                imageUrl[i] = Configurations.SERVER_URL + "uploads/" + JSONimageURL.getString(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //LOAD MULTIPLE PROPERTIES----------------------------------------------------------------------

    /**
     * Load Properties from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param downloadedListener
     */
    public static void loadProperties(FragmentActivity activity, int offset, int limit, String search, final onPropertiesDownloadedListener downloadedListener) {
        loadProperties(activity, offset, limit, search, "", downloadedListener);
    }

    /**
     * Load properties from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param category
     * @param downloadedListener
     */
    public static void loadProperties(final FragmentActivity activity, int offset, int limit, String search, String category, final onPropertiesDownloadedListener downloadedListener) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("search", search);
        params.put("category", category);
        loadProperties(activity, offset, limit, params, downloadedListener);
    }

    /**
     * Load properties by ids (for favorites)
     *
     * @param activity
     * @param ids
     * @param downloadedListener
     */
    public static void loadProperties(final FragmentActivity activity, final int[] ids, final onPropertiesDownloadedListener downloadedListener) {
        JSONArray idsJSON = new JSONArray();
        for (int i = 0; i < ids.length; i++) {
            idsJSON.put(ids[i]);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", idsJSON.toString());
        loadProperties(activity, 0, 100, params, downloadedListener);
    }

    /**
     * Load properties from server with parameter hashmap
     * @param activity
     * @param offset
     * @param limit
     * @param params
     * @param downloadedListener
     */
    public static void loadProperties(final FragmentActivity activity, int offset, int limit, Map<String, String> params, final onPropertiesDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/properties/" + offset + "/" + limit + "?" + urlEncodeUTF8(params);
        Log.e("URL", url);
        final Cache cache = new Cache(activity);
        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return properties
                decodeProperties(responseStr, downloadedListener);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
                //Try to load from cache else view warning
                String responseStr = cache.load(url);
                if (responseStr != null) {
                    System.out.println("loading cached data: " + responseStr);
                    decodeProperties(responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);

            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }



    /**
     * Decode properties received from server or cache
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeProperties(String responseStr, final onPropertiesDownloadedListener downloadedListener) {
        try {
            JSONArray response = new JSONArray(responseStr);
            System.out.println(response.toString(2));
            List<Property> properties = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                properties.add(new Property(jsonObject));
            }
            downloadedListener.onPropertiesDownloaded(properties);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //LOAD SINGLE PROPERTY--------------------------------------------------------------------------

    /**
     * Load a single property by id
     *
     * @param activity
     * @param id
     * @param downloadedListener
     */
    public static void loadProperty(final FragmentActivity activity, int id, final onPropertyDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/property/" + id;
        final Cache cache = new Cache(activity);
        Log.e("URL", url);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return properties
                decodeProperty(activity, responseStr, downloadedListener);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
                //Try to load from cache else view warning
                String responseStr = cache.load(url);
                if (responseStr != null) {
                    System.out.println("loading cached data: " + responseStr);
                    decodeProperty(activity, responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(req);
    }


    /**
     * Decode a single property
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeProperty(final FragmentActivity activity, String responseStr, final onPropertyDownloadedListener downloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            final Property property = new Property(response);
            PropertyDetail.loadPropertyDetail(activity, PropertyDetail.PROPERTY_STATUS, property.status, new PropertyDetail.onDownloadedListener() {
                @Override
                public void onDownloaded(PropertyDetail item) {
                    property.statusName = item.name;
                    PropertyDetail.loadPropertyDetail(activity, PropertyDetail.PROPERTY_TYPE, property.type, new PropertyDetail.onDownloadedListener() {
                        @Override
                        public void onDownloaded(PropertyDetail item) {
                            property.typeName = item.name;
                            PropertyDetail.loadPropertyDetail(activity, PropertyDetail.PROPERTY_ENERGY_RATING, property.energy, new PropertyDetail.onDownloadedListener() {
                                @Override
                                public void onDownloaded(PropertyDetail item) {
                                    property.energyName = item.name;
                                    downloadedListener.onPropertyDownloaded(property);
                                }
                            });
                        }
                    });
                }
            });

        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get property features in text format to display
     * @return
     */
    public String getFeatures(Context context) {
        String features = "";

        features += String.format(context.getResources().getString(R.string.property_type_display),typeName)+ "\r\n";
        features += String.format(context.getResources().getString(R.string.property_energy_display),energyName) + "\r\n";
        features += String.format(context.getResources().getString(R.string.property_yearbuilt_display),yearbuilt) + "\r\n";
        return features;
    }

    /**
     * Get address formatted to display
     * @return
     */
    public String getAddress() {
        String features = "";

        if (address.length() > 0)
            features += address + "\r\n";
        if (county.length() > 0)
            features += county + "\r\n";
        if (city.length() > 0)
            features += city + "\r\n";
        if (zipcode.length() > 0)
            features += zipcode + "\r\n";
        return features;
    }


    //PROPERTY STATISTICS---------------------------------------------------------------------------

    public void favorite(Context context) {
        send(context, "favorite/" + id);
    }

    public void shared(Context context) {
        send(context, "shared/" + id);
    }

    public void viewed(Context context) {
        send(context, "viewed/" + id);
    }


    public static void send(Context context, String data) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Configurations.SERVER_URL + "api/property/" + data;

        StringRequest arrayreq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {

            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    //PROPERTY FAVORITE-----------------------------------------------------------------------------

    /**
     * Set this property as favorite
     * @param context
     * @param isFavorite
     */
    public void setFavorite(Context context, boolean isFavorite) {

        if (isFavorite) {
            favorite(context);
            Save.addToArray(id, "favorites", context);
        } else {
            int[] allFavorites = Save.loadIntArray("favorites", context);
            for (int i = 0; i < allFavorites.length; i++) {
                if (allFavorites[i] == this.id) {
                    Save.removeFromIntArray(i, "favorites", context);
                    return;
                }
            }
        }
    }

    /**
     * Is this property favorited?
     * @param context
     * @return
     */
    public boolean isFavorite(Context context) {
        //get favorites
        int[] allFavorites = Save.loadIntArray("favorites", context);
        for (int i = 0; i < allFavorites.length; i++) {
            if (allFavorites[i] == this.id) {
                return true;
            }
        }
        return false;
    }


    /**
     * Get a list of all the favorite properties
     * @param activity
     * @param downloadedListener
     */
    public static void getFavoriteProperties(FragmentActivity activity, final onPropertiesDownloadedListener downloadedListener) {
        int[] allFavorites = Save.loadIntArray("favorites", activity);
        if (allFavorites.length > 0)
            loadProperties(activity, allFavorites, downloadedListener);
        else
            downloadedListener.onPropertiesDownloaded(new ArrayList<Property>());
    }

    //PROPERTY FROM INTENT--------------------------------------------------------------------------

    /**
     * Gets a property id from intent.
     *
     * @param intent
     * @param savedInstanceState
     * @return
     */
    public static int getPropertyIdFromIntent(Intent intent, Bundle savedInstanceState) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String propertyIdstr = data.substring(data.lastIndexOf("/") + 1);
            try {
                int propertyId = Integer.parseInt(propertyIdstr);
                return propertyId;
            } catch (Exception e) {

            }
        }

        //from bundle
        if (savedInstanceState == null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return extras.getInt(ITEM_KEY, -10);
            }
        } else {
            if (savedInstanceState.containsKey(ITEM_KEY))
                return (int) savedInstanceState.getSerializable(ITEM_KEY);
        }

        //no id found
        return -10;
    }

    //PROPERTY SHARE--------------------------------------------------------------------------------

    /**
     * Share on social media
     */
    public void share(Activity activity) {
        shared(activity);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.share_message) + " " + "http://" + activity.getResources().getString(R.string.deep_link) + "/" + id);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, "Share via"));
    }

}
