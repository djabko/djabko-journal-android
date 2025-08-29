package com.example.djabko_journal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.djabko_journal.databinding.EntriesFragmentBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;

public class EntriesFragment extends Fragment {

    private EntriesFragmentBinding binding;

    public static void addLog(View view, Message m) {
        LinearLayout parent = view.findViewById(R.id.entries_layout);
        parent.addView(m.toLinearLayout(view.getContext(), true));
    }

    protected void loadLogs(View view) {
        if (MainActivity.getNotebookKey() == null) return;

        LinearLayout parent = view.findViewById(R.id.entries_layout);
        parent.removeAllViews();

        TextView logView = new TextView(getContext());
        parent.addView(logView);

        logView.append("Attempting to read logs...\n");

        boolean success = DjabkoJournal.read(logView,
                json -> {
                    try {
                        JSONArray items = json.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            Message m = new Message(item);

                            m.message = MainActivity.jcipher.decrypt(m.message);
                            addLog(view, m);
                        }

                    } catch (Exception e) {
                        logView.append("Error: " + e + "\n\n");
                    }
                },
                verror -> logView.append("FAILED " + verror.toString() + "\n"));

        if (!success) {
            logView.append("SUPER ERROR\n");
        }

        logView.setText("");
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = EntriesFragmentBinding.inflate(inflater);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.entriesFragment = this;

        loadLogs(getView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}