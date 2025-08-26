package com.example.djabko_journal;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

public interface JsonSuccessCallback {
    void onSuccess(JSONObject json);
}

