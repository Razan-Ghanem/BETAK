package com.neurondigital.estate;

import android.content.Context;
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
import java.util.List;

/**
 * Created by melvin on 25/09/2016.
 * This is a detail item. The class contains the functionality to download a detail object from the server via JSON
 */
public class PropertyDetail {

    //Detail elements
    public int id;
    public String name;

    public static String PROPERTY_STATUS = "propertystatus", PROPERTY_TYPE = "propertytype", PROPERTY_ENERGY_RATING = "propertyefficiency";
    public static String PROPERTY_STATUSES = "propertystatuses", PROPERTY_TYPES = "propertytypes", PROPERTY_ENERGY_RATINGS = "propertyefficiencies";


    /**
     * Listens for when details are downloaded
     */
    interface onMultipleDownloadedListener {
        void onMultipleDownloaded(List<PropertyDetail> item);
    }

    /**
     * Listens for when a detail is downloaded
     */
    interface onDownloadedListener {
        void onDownloaded(PropertyDetail item);
    }


    /**
     * Constructor that generates a detail from JSON
     *
     * @param JSONCategory
     */
    public PropertyDetail(JSONObject JSONCategory) {
        try {
            id = JSONCategory.getInt("id");
            name = JSONCategory.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Load all property details from server
     * @param context
     * @param detailName
     * @param search
     * @param downloadedListener
     */
    public static void loadPropertyDetails(final Context context, String detailName, String search, final onMultipleDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        final String url = Configurations.SERVER_URL + "api/" + detailName + "?search=" + search;
        final Cache cache = new Cache(context);
        //generate request
        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode details
                decodePropertyDetails(responseStr, downloadedListener);
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
                    decodePropertyDetails(responseStr, downloadedListener);
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }

    /**
     * Decode property details
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodePropertyDetails(String responseStr, onMultipleDownloadedListener downloadedListener) {
        try {
            JSONArray response = new JSONArray(responseStr);
            //parse categories
            List<PropertyDetail> details = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                details.add(new PropertyDetail(jsonObject));
            }
            //callback categories
            downloadedListener.onMultipleDownloaded(details);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Load a single property detail from server
     * @param context
     * @param detailName
     * @param id
     * @param downloadedListener
     */
    public static void loadPropertyDetail(final Context context, String detailName, int id, final onDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        final String url = Configurations.SERVER_URL + "api/" + detailName + "/" + id;
        final Cache cache = new Cache(context);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return properties
                decodePropertyStatus(context, responseStr, downloadedListener);
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
                    decodePropertyStatus(context, responseStr, downloadedListener);
                }
            }
        });
        // Add the request to the RequestQueue.
        queue.add(req);
    }


    /**
     * Decode a single property detail
     * @param context
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodePropertyStatus(final Context context, String responseStr, final onDownloadedListener downloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            final PropertyDetail propertydetails = new PropertyDetail(response);
            downloadedListener.onDownloaded(propertydetails);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
