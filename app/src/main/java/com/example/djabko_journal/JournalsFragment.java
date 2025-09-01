package com.example.djabko_journal;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.djabko_journal.databinding.FragmentSecondBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.util.HashSet;

public class JournalsFragment extends Fragment {

    private FragmentSecondBinding binding;

    private void addNewJournal(View view) {
        Context context = view.getContext();

        final EditText input = new EditText(context);
        input.setHint("Enter Journal Key");

        new MaterialAlertDialogBuilder(context)
                .setTitle("Journal Key")
                .setView(input)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String text = input.getText().toString();
                    if (!text.trim().isEmpty()) {
                        View parent = (View) view.getParent();
                        LinearLayout layout = parent.findViewById(R.id.journals_list);
                        if (!insertJournal(context, layout, text)) Log.println(Log.ERROR, "JournalsFragment", "Couldn't insert Journal...");
                        MainActivity.setNotebook(new Journal(text));
                        Log.println(Log.INFO, "JournalsFragment", "Journal selected: " + MainActivity.getNotebook().name);

                        MainActivity.reloadLogs();
                    } else {
                        Snackbar.make(view, "Entry cannot be empty", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private boolean insertJournal(Context ctx, LinearLayout layout, String key) {
        if (ctx == null || layout == null) return false;

        TextView tv = new TextView(ctx);

        tv.setGravity(Gravity.START);
        tv.setText(key);
        tv.setPadding(32,32,0,0);
        tv.setOnClickListener((v) -> {
            MainActivity.setNotebook(new Journal(key));
        });
        tv.setOnLongClickListener((v) -> removeJournal(v, key));

        layout.addView(tv);

        return true;
    }

    private boolean removeJournal(View view, String key) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null || key == null) return false;

        MainActivity.removeNotebook(MainActivity.journals.get(key));
        parent.removeView(view);
        return true;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = getContext();
        LinearLayout layout = view.findViewById(R.id.journals_list);

        if (0 < MainActivity.notebookKeys.size())
            for (String key : MainActivity.notebookKeys)
                insertJournal(ctx, layout, key);

        binding.buttonSecond.setOnClickListener(this::addNewJournal
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}