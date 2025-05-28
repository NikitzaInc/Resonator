package com.resistorm.resonator.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkManager {
    private final Context context;
    private final String serverUrl;
    private final RequestQueue requestQueue;

    public NetworkManager(Context context, String serverUrl) {
        this.context = context;
        this.serverUrl = serverUrl;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void uploadImage(String base64Image, String timestamp,
                            Response.Listener<String> responseListener,
                            Response.ErrorListener errorListener) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("image", base64Image);
            jsonBody.put("timestamp", timestamp);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl,
                    responseListener,
                    errorListener) {

                @Override
                public byte[] getBody() {
                    return jsonBody.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            errorListener.onErrorResponse(new VolleyError("Error creating JSON: " + e.getMessage()));
        }
    }
}
