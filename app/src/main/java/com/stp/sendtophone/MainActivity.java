package com.stp.sendtophone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Features: Send from device

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.RecyclerViewClickListener, FirebaseAuth.AuthStateListener {
    private static final int RC_SIGN_IN = 808;
    private RecyclerViewAdapter adapter;
    private List<String> messageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private AlertDialog dialog;
    AlertDialog.Builder builder;

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            messageList = SharedPrefHelper.getMsgArrayList(MainActivity.this);
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
                MODE_PRIVATE);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (isSignedIn()) { attachRecyclerViewAdapter(); }
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }

    private void handleSignInResponse(int resultCode, @Nullable Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            attachRecyclerViewAdapter();
            finish();
        } else {
            // Sign in failed
            if (response == null) {
                return;
            }
            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }
            showSnackbar(R.string.unknown_error);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (isSignedIn()) {
            attachRecyclerViewAdapter();
        } else {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                            Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            )
                    ).build(), RC_SIGN_IN);
        }
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void launchSettings(){
        SettingsFragment settingsFragment = new SettingsFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm
                .beginTransaction();
        recyclerView.setVisibility(View.GONE);
        fragmentTransaction.replace(R.id.frame_layout,
                settingsFragment).addToBackStack(null).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                launchSettings();
                return true;
            case R.id.action_delete_all:
                showDeletionDialog(recyclerView);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearMessage(int position){
        SharedPrefHelper.clearMessages(this, position);
        showSnackbar(R.string.delete_message_toast);
    }

    private void clearAllMessages() {
        SharedPrefHelper.clearMessages(this);
        showSnackbar(R.string.delete_all_toast);
    }

    @Override
    public void onBackPressed() {
        recyclerView.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        super.onBackPressed();
    }


    private void attachRecyclerViewAdapter() {
        messageList = SharedPrefHelper.getMsgArrayList(MainActivity.this);
        adapter = new RecyclerViewAdapter(this, messageList, this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(0);
            }
        });
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
        SingleMessageFragment messageFragment = new SingleMessageFragment();
        Bundle b = new Bundle();
        b.putString(getString(R.string.selected_message_key), adapter.getItem(position));
        messageFragment.setArguments(b);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm
                .beginTransaction();
        getSupportActionBar().setTitle(getString(R.string.received));
        fragmentTransaction.replace(R.id.frame_layout,
                messageFragment).addToBackStack(null).commit();
    }

    public void closeMessage() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SingleMessageFragment messageFragment = (SingleMessageFragment) fragmentManager
                .findFragmentById(R.id.frame_layout);
        if (messageFragment != null) {
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(messageFragment).commit();
        }
    }

    private void showSnackbar(@StringRes int snackMessage) {
        Snackbar.make(findViewById(android.R.id.content), snackMessage, Snackbar.LENGTH_LONG).show();
    }

    public void showDeletionDialog(View view){
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_deletion_alert).
                setMessage(R.string.delete_all_alert);
        builder.setPositiveButton(R.string.continue_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                SharedPrefHelper.clearMessages(MainActivity.this);
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public void showDeletionDialog(View view, int selection) {
        final int selected = selection;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_deletion_alert).
                setMessage(R.string.delete_one_alert);
        builder.setPositiveButton(R.string.continue_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                SharedPrefHelper.clearMessages(MainActivity.this, selected);
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

}

