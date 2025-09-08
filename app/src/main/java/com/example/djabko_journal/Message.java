package com.example.djabko_journal;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public String notebook = null;
    public String date = null;
    public String time = null;
    public String datetime = null;
    public String message = null;
    public String author = null;
    public String tag1 = null;
    public String tag2 = null;
    public String tag3 = null;
    public String tag4 = null;

    public Message () {}

    public Message(String message) {
        this.message = message;
    }

    public Message (JSONObject json) throws JSONException {
        String datetime = json.getString("Datetime");
        int idx = datetime.indexOf(' ');

        this.date = datetime.substring(0, idx);
        this.time = datetime.substring(idx + 1, datetime.indexOf('.'));
        this.datetime = date + " " + time;
        this.notebook = json.getString("Notebook");
        this.message = json.getString("Message");

        if (json.has("Author")) this.author = json.getString("Author");
        if (json.has("Tag1")) this.tag1 = json.getString("Tag1");
        if (json.has("Tag2")) this.tag2 = json.getString("Tag2");
        if (json.has("Tag3")) this.tag3 = json.getString("Tag3");
        if (json.has("Tag4")) this.tag4 = json.getString("Tag4");
    }

    public Message (String notebook, String date, String time, String message) {
        this.notebook = notebook;
        this.date = date;
        this.time = time;
        this.datetime = date + " " + time;
        this.message = message;
    }

    public Message (String notebook, String date, String time, String message, String author, String tag1, String tag2, String tag3, String tag4) {
        this.notebook = notebook;
        this.date = date;
        this.time = time;
        this.datetime = date + " " + time;
        this.message = message;
        this.author = author;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
    }

    @NonNull
    public String toString() {
        return datetime + "\t" + message;
    }

    public JSONObject toJson() {

        try {
            JSONObject json = new JSONObject();

            json.put("Notebook", notebook);
            json.put("Datetime", datetime);
            json.put("Message", message);

            if (author != null) json.put("Author", author);
            if (tag1 != null) json.put("Tag1", tag1);
            if (tag2 != null) json.put("Tag2", tag2);
            if (tag3 != null) json.put("Tag3", tag3);
            if (tag4 != null) json.put("Tag4", tag4);

            return json;
        } catch (JSONException e) {
            Log.println(Log.ERROR, "JSON", Log.getStackTraceString(e));
            return null;
        }
    }

    @NonNull
    public LinearLayout toLinearLayout(Context ctx, boolean isSelectable) {
        TextView datetime = new TextView(ctx);
        TextView message = new TextView(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        datetime.setText(this.datetime);
        datetime.setLayoutParams(params);
        datetime.setGravity(Gravity.START);
        datetime.setPadding(0,0,0,16);
        datetime.setTextIsSelectable(isSelectable);
        message.setText(this.message);
        message.setLayoutParams(params);
        message.setGravity(Gravity.START);
        message.setTextIsSelectable(isSelectable);

        LinearLayout entry = new LinearLayout(ctx);

        entry.setOrientation(LinearLayout.HORIZONTAL);
        entry.setWeightSum(2);
        entry.addView(datetime);
        entry.addView(message);

        return entry;
    }

    public LinearLayout toLinearLayout(Context ctx) {
        return toLinearLayout(ctx, false);
    }
}