package com.example.djabko_journal;

import android.content.Context;
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
    }

    public Message (String notebook, String date, String time, String message) {
        this.notebook = notebook;
        this.date = date;
        this.time = time;
        this.datetime = date + " " + time;
        this.message = message;
    }

    @NonNull
    public String toString() {
        return this.datetime + "\t" + this.message;
    }

    @NonNull
    public LinearLayout toLinearLayout(Context ctx) {
        TextView datetime = new TextView(ctx);
        TextView message = new TextView(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        datetime.setText(this.datetime);
        datetime.setLayoutParams(params);
        datetime.setGravity(Gravity.START);
        datetime.setPadding(0,0,0,16);
        message.setText(this.message);
        message.setLayoutParams(params);
        message.setGravity(Gravity.START);

        LinearLayout entry = new LinearLayout(ctx);

        entry.setOrientation(LinearLayout.HORIZONTAL);
        entry.setWeightSum(2);
        entry.addView(datetime);
        entry.addView(message);

        return entry;
    }
}