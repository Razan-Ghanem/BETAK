package com.neurondigital.estate;

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

/**
 * Created by melvin on 25/03/2017.
 * This handles the downloading of a Agents
 */
public class Agent {
    public int id;
    public String name;
    public String imageUrl;
    public String tel, email;

    interface onAgentDownloadedListener {
        void onAgentDownloaded(Agent agent);
    }

    interface onAgentsDownloadedListener {
        void onAgentsDownloaded(List<Agent> agents);
    }

    public Agent(int id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Agent(String name) {
        this.name = name;
    }


    /**
     * Decode agent from JSON
     *
     * @param JSONAgent
     */
    public Agent(JSONObject JSONAgent) {
        try {
            id = JSONAgent.getInt("id");
            name = JSONAgent.getString("name");
            tel = JSONAgent.getString("tel");
            email = JSONAgent.getString("email");
            JSONArray JSONimageURL = new JSONArray(JSONAgent.getString("image"));
            if(JSONimageURL.length()>0){
                imageUrl = Configurations.SERVER_URL + "uploads/" + JSONimageURL.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //LOAD MULTIPLE AGENTS-------------------------------------------------------------------------

    /**
     * Load agents from server
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param downloadedListener
     */
    public static void loadAgents(final FragmentActivity activity, int offset, int limit, String search, final onAgentsDownloadedListener downloadedListener) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("search", search);
        loadAgents(activity, offset, limit, params, downloadedListener);
    }


    /**
     * Load agents by ids (for favorites)
     *
     * @param activity
     * @param ids
     * @param downloadedListener
     */
    public static void loadAgents(final FragmentActivity activity, final int[] ids, final onAgentsDownloadedListener downloadedListener) {
        JSONArray idsJSON = new JSONArray();
        for (int i = 0; i < ids.length; i++) {
            idsJSON.put(ids[i]);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", idsJSON.toString());
        loadAgents(activity, 0, 100, params, downloadedListener);
    }

    /**
     * Load agents from server
     * @param activity
     * @param offset
     * @param limit
     * @param params
     * @param downloadedListener
     */
    public static void loadAgents(final FragmentActivity activity, int offset, int limit, Map<String, String> params, final onAgentsDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/agents/" + offset + "/" + limit + "?" + urlEncodeUTF8(params);
        Log.e("URL", url);
        final Cache cache = new Cache(activity);
        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return agents
                decodeAgents(responseStr, downloadedListener);
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
                    decodeAgents(responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);

            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    /**
     * Decode agents received from server or cache
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeAgents(String responseStr, final onAgentsDownloadedListener downloadedListener) {
        try {
            JSONArray response = new JSONArray(responseStr);
            System.out.println(response.toString(2));
            List<Agent> agents = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                agents.add(new Agent(jsonObject));
            }
            downloadedListener.onAgentsDownloaded(agents);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //LOAD SINGLE AGENT---------------------------------------------------------------------------

    /**
     * Load a single agent by id
     *
     * @param activity
     * @param id
     * @param downloadedListener
     */
    public static void loadAgent(final FragmentActivity activity, int id, final onAgentDownloadedListener downloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/agent/" + id;
        final Cache cache = new Cache(activity);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return agents
                decodeAgent(activity, responseStr, downloadedListener);
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
                    decodeAgent(activity, responseStr, downloadedListener);
                } else
                    Functions.noInternetAlert(activity);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(req);
    }

    /**
     * Decode a single agent
     *
     * @param responseStr
     * @param downloadedListener
     */
    public static void decodeAgent(final FragmentActivity activity, String responseStr, final onAgentDownloadedListener downloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            final Agent agent = new Agent(response);
            downloadedListener.onAgentDownloaded(agent);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
