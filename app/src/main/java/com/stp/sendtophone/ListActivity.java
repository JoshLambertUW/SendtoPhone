package com.stp.sendtophone;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.util.ArrayList;
import java.util.List;

// Features: Send from device

public class ListActivity extends AppCompatActivity implements RecyclerViewAdapter.RecyclerViewClickListener
        , SingleMessageFragment.SingleMessageDeletionListener {
    private RecyclerViewAdapter adapter;
    private List<String> messageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private SharedPreferences sharedPreferences;
    private AlertDialog dialog;
    AlertDialog.Builder builder;

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            messageList.clear();
            List<String> updatedMessages = SharedPrefHelper.getMsgArrayList(ListActivity.this);
            messageList.addAll(updatedMessages);
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        recyclerView = findViewById(R.id.recyclerView);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
                MODE_PRIVATE);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
        attachRecyclerViewAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void launchSettings() {
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
            case R.id.action_sort_list:
                reverseList();
                return true;
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

    private void clearMessages(int position) {
        SharedPrefHelper.clearMessages(this, position);
        showSnackbar(R.string.delete_message_toast);
    }

    private void clearMessages() {
        SharedPrefHelper.clearMessages(this);
        showSnackbar(R.string.delete_all_toast);
    }

    private void reverseList() {
        linearLayoutManager.setReverseLayout(!linearLayoutManager.getReverseLayout());
        linearLayoutManager.setStackFromEnd(!linearLayoutManager.getStackFromEnd());
    }

    @Override
    public void onBackPressed() {
        recyclerView.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        super.onBackPressed();
    }

    private void attachRecyclerViewAdapter() {
        messageList = SharedPrefHelper.getMsgArrayList(ListActivity.this);
        adapter = new RecyclerViewAdapter(this, messageList, this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        SingleMessageFragment messageFragment = new SingleMessageFragment();
        Bundle b = new Bundle();
        b.putString(getString(R.string.selected_message_key), adapter.getItem(position));
        b.putInt(getString(R.string.selected_message_position), position);
        messageFragment.setArguments(b);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm
                .beginTransaction();
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

    public void showDeletionDialog(View view) {
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_deletion_alert).
                setMessage(R.string.delete_all_alert);
        builder.setPositiveButton(R.string.continue_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                clearMessages();
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
                clearMessages(selected);
                getSupportFragmentManager().popBackStack();
                getSupportActionBar().setDisplayShowCustomEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof SingleMessageFragment) {
            SingleMessageFragment singleMessageFragment = (SingleMessageFragment) fragment;
            singleMessageFragment.setOnMessageDeletionListener(this);
        }
    }

    public void singleMessageDeletion(int position) {
        showDeletionDialog(recyclerView, position);
    }
}


