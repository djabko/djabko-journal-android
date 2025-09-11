package com.example.djabko_journal;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class DjabkoJournal {
    private static Context m_ctx;
    private static RequestQueue m_queue;
    private static boolean m_initialized = false;

    private static void display(View view, String s) {
        Snackbar.make(view, s, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show();
    }

    private static void initialize(View view) {
        if (m_initialized) return;

        m_ctx = view.getContext().getApplicationContext();
        m_queue = Volley.newRequestQueue(m_ctx);
        m_initialized = true;
    }

    public static void create(View view) {
        initialize(view);

        StringRequest request = new StringRequest(
            Request.Method.POST,
            "https://djabko.com/journal/create",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    display(view, s);
                }
            }, volleyError -> display(view, volleyError.toString())
        );

        m_queue.add(request);
    }

    public static void log(View view, Message message, Response.Listener<JSONObject> successCallback, Response.ErrorListener failureCallback) {
        initialize(view);
        display(view, "Logging '" + message.message + "'");

        // TODO: JSONObject body = message.toJson();
        JSONObject body = new JSONObject();

        try {
            body.put(JournalConstants.NOTEBOOK.raw, MainActivity.getNotebook().name);
            body.put(JournalConstants.MESSAGE.raw, MainActivity.getNotebook().jcipher.encrypt(message.message));

            if (message.author != null)
                body.put(JournalConstants.AUTHOR.raw, message.author);

            if (message.tag1 != null)
                body.put(JournalConstants.TAG1.raw, message.tag1);

            if (message.tag2 != null)
                body.put(JournalConstants.TAG2.raw, message.tag2);

            if (message.tag3 != null)
                body.put(JournalConstants.TAG3.raw, message.tag3);

            if (message.tag4 != null)
                body.put(JournalConstants.TAG4.raw, message.tag4);

        } catch (Exception e) {
            display(view, "Error: " + e);
            Log.println(Log.ERROR, "DjabkoJournal", Log.getStackTraceString(e));
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://djabko.com/journal/log",
                body,
                successCallback,
                failureCallback
        );

        m_queue.add(request);
    }

    public static void log(View view, String text, Response.Listener<JSONObject> successCallback, Response.ErrorListener failureCallback) {
        log(view, new Message(text), successCallback, failureCallback);
    }

    public static void log(View view, Message message) {
        log(
                view,
                message,
                (json) -> {
                    display(view, "Logged: " + json.toString() + "\n");
                },
                (e) -> {
                    display(view, "VolleyError: " + e + "\n");
                });
    }

    public static void log(View view, String text) {
        log(view, new Message(text));
    }

    public static boolean read(View view, JSONObject query, JsonSuccessCallback onSuccess, JsonErrorCallback onError) {
        if (query == null) return false;

        initialize(view);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://djabko.com/journal/read",
                query,
                onSuccess::onSuccess,
                onError::onError
        );

        m_queue.add(request);

        return true;
    }

    public static boolean read(View view, JsonSuccessCallback onSuccess, JsonErrorCallback onError) {
        JSONObject query = new JSONObject();

        try {
            query.put(JournalConstants.NOTEBOOK.raw, MainActivity.getNotebook().name);
        } catch (JSONException e) {
            display(view, "Error: " + e);
            Log.println(Log.ERROR, "DjabkoJournal", Log.getStackTraceString(e));
            return false;
        }

        return read(view, query, onSuccess, onError);
    }
}