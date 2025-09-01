package com.example.djabko_journal;

import android.util.Log;

public class Journal {
    JournalCipher jcipher;
    public String name;
    public Journal(String name) {
        this.name = name;

        try {
            this.jcipher = new JournalCipher(name, null);
        } catch (Exception e) {
            Log.println(Log.ERROR, this.getClass().toString(), Log.getStackTraceString(e));
        }
    }
}