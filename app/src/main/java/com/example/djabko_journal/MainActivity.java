package com.example.djabko_journal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.util.Base64;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.djabko_journal.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME_NOTEBOOKS = "Notebooks";
    private static final String PREF_KEY_NOTEBOOKS = "Keys";
    private static Journal journalSelected;
    public static Set<String> notebookKeys;
    public static HashMap<String, Journal> journals = new HashMap<String, Journal>();

    public static EntriesFragment entriesFragment;
    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawerLayout;
    private static MainActivity singleton;

    private void inputHandler(View view, Message message) {

        if (message.message.isEmpty()) {
            Snackbar.make(view, "Entry cannot be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }

        DjabkoJournal.log(
                view,
                message,
                (json) -> {
                    try {
                        Message response = new Message(json);
                        response.message = message.message; // Server will send ciphertext
                        addLog(response);
                    } catch (JSONException e) {
                        Snackbar.make(view, e.toString(), Snackbar.LENGTH_SHORT).show();
                        Log.println(Log.ERROR, this.getClass().toString(), Log.getStackTraceString(e));
                    }
                },
                (e) -> Snackbar.make(view, e.toString(), Snackbar.LENGTH_SHORT).show()
        );
    }

    private EditText buildEditTextView(Context context, LinearLayout layout, String hint, int ems, LinearLayout.LayoutParams params) {
        EditText view = new EditText(context);

        if (hint != null) view.setHint(hint);
        if (params != null) view.setLayoutParams(params);
        if (layout != null) layout.addView(view);
        if (ems > 1) view.setEms(ems);

        return view;
    }

    private void readMessage(View view) {
        if (MainActivity.getNotebook() == null) {
            Snackbar.make(view, "No journal selected...", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Context context = view.getContext();

        int ems = 5;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout layout = new LinearLayout(context);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        final EditText message = buildEditTextView(context, layout, "Enter Log", 0, null);
        final EditText author = buildEditTextView(context, layout, "Author", ems, params);
        final EditText tag1 = buildEditTextView(context, layout, "Tag 1", ems, params);
        final EditText tag2 = buildEditTextView(context, layout, "Tag 2", ems, params);
        final EditText tag3 = buildEditTextView(context, layout, "Tag 3", ems, params);
        final EditText tag4 = buildEditTextView(context, layout, "Tag 4", ems, params);

        new MaterialAlertDialogBuilder(context)
            .setTitle("New Journal Entry")
            .setView(layout)
            .setPositiveButton("Submit", (dialogInterface, which) -> {
                Message m = new Message();
                m.message = message.getText().toString().trim();
                m.author = author.getText().toString().trim();
                m.tag1 = tag1.getText().toString().trim();
                m.tag2 = tag2.getText().toString().trim();
                m.tag3 = tag3.getText().toString().trim();
                m.tag4 = tag4.getText().toString().trim();

                inputHandler(view, m);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addLog(Message m) {
        Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHost != null) {
            List<Fragment> children = navHost.getChildFragmentManager().getFragments();
            for (Fragment child : children) {
                if (child instanceof EntriesFragment) {
                    View root = child.getView();
                    assert root != null;
                    EntriesFragment.addLog(root, m);

                    break;
                }
            }
        }
    }

    private void injectFragment(int layoutID, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(layoutID, fragment);
        transaction.commit();
    }

    private void loadNotebooksPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME_NOTEBOOKS, Context.MODE_PRIVATE);
        Set<String> ss = sharedPreferences.getStringSet(PREF_KEY_NOTEBOOKS, new HashSet<>());

        notebookKeys = new HashSet<>(ss);
    }

    private void flushNotebooksPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME_NOTEBOOKS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(PREF_KEY_NOTEBOOKS, notebookKeys);
        editor.apply();
    }

    public static Journal getNotebook() {
        return journalSelected;
    }

    public static void setNotebook(Journal journal) {
        journals.put(journal.name, journal);

        if (!notebookKeys.contains(journal.name)) {
            notebookKeys.add(journal.name);
            singleton.flushNotebooksPref();
        }

        Log.println(Log.INFO, "MainActivity", "Set notebook to '" + journal.name + "'!!!");

        journalSelected = journal;

        reloadLogs();
    }

    public static boolean removeNotebook(Journal journal) {
        journals.remove(journal.name);
        if (journal == null || !journals.containsKey(journal.name)) return false;

        notebookKeys.remove(journal.name);
        singleton.flushNotebooksPref();

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        drawerLayout = findViewById(R.id.drawer_layout);
        assert drawerLayout != null;

        singleton = this;

        injectFragment(R.id.left_drawer, new JournalsFragment());
        loadNotebooksPref();

        binding.fab.setOnClickListener(this::readMessage);
    }

    public static void reloadLogs() {
        assert entriesFragment != null;
        entriesFragment.loadLogs(entriesFragment.getView());
        singleton.drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_secret_key).setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            AlertDialog dialog;

            if (journalSelected == null) {
                builder.setTitle("No journal selected...");
                builder.setPositiveButton("Cancel", null);
                dialog = builder.create();

            } else {
                builder.setTitle("Enter secret symmetric key.");
                JournalCipher jcipher = journalSelected.jcipher;
                final EditText input = new EditText(this);

                byte[] key = jcipher.getKey().getEncoded();
                String text = (key == null) ? "" : Base64.encodeToString(key, Base64.NO_WRAP);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(text);
                builder.setView(input);
                builder.setPositiveButton("Set", null);

                dialog = builder.create();
                dialog.setOnShowListener(d -> {
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(v -> {
                        String s_key = input.getText().toString();

                        JournalCipherError result = jcipher.setKey(s_key);

                        if (result == JournalCipherError.OK)
                            dialog.dismiss();

                        else if (result == JournalCipherError.BASE64_DECODING_ERROR)
                            input.setText("Invalid base64 string...");

                        else if (result == JournalCipherError.KEYSTORE_EXCEPTION) {
                            input.setText("Keystore rejected key persistence, however it may still be used for decryption.");
                            reloadLogs();

                        } else
                            input.setText("Unexpected error...");
                    });
                });
            }

            dialog.show();

            return true;
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}