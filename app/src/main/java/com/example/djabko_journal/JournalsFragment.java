package com.example.djabko_journal;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.style.BackgroundColorSpan;
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
                        MainActivity.setNotebookKey(text);
                        MainActivity.reloadLogs();

                        View parent = (View) view.getParent();
                        LinearLayout layout = parent.findViewById(R.id.journals_list);
                        insertJournal(context, layout, text);
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

    private void insertJournal(Context ctx, LinearLayout layout, String key) {
        if (ctx == null || layout == null) return;

        TextView tv = new TextView(ctx);

        tv.setGravity(Gravity.START);
        tv.setText(key);
        tv.setPadding(32,32,0,0);
        tv.setOnClickListener((v) -> {
            MainActivity.setNotebookKey(key);
        });
        tv.setOnLongClickListener((v) -> removeJournal(v, key));

        layout.addView(tv);
    }

    private boolean removeJournal(View view, String key) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return false;

        MainActivity.removeNotebookKey(key);
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