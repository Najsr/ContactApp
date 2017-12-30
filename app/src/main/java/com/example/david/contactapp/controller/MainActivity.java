package com.example.david.contactapp.controller;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.david.contactapp.ContactAdapter;
import com.example.david.contactapp.PermissionsHelper;
import com.example.david.contactapp.R;
import com.example.david.contactapp.model.Contact;
import com.melnykov.fab.FloatingActionButton;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.contactList)
    ListView contactList;

    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;

    private String numberToCall = "";

    public static final String EXTRA_MESSAGE = "secretMessage";

    public static final int RETURN_CODE = 155;

    private List<Contact> contacts;

    private ContactAdapter contactAdapter;

    private ArrayList checkedItems = new ArrayList<>(); // for holding list item ids

    private Realm realm;

    private boolean deleting = false;

    private Map<Contact, Integer> selectedContacts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        ButterKnife.bind(this);
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        contacts = new ArrayList<>(realm.where(Contact.class).findAll());
        if(contacts.size() == 0)
            contacts = new ArrayList<>();
        else
            Collections.sort(contacts, new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact t1) {
                    return contact.getName().compareToIgnoreCase(t1.getName());
                }
            });
        contactAdapter = new ContactAdapter(this, R.layout.contacts_layout, contacts);
        contactList.setAdapter(contactAdapter);
    }


    @OnItemClick(R.id.contactList)
    public void itemClicked(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Contact contact = (Contact) arg0.getItemAtPosition(position);
        if(deleting) {
            if(!selectedContacts.containsKey(contact)) {
                selectedContacts.put(contact, position);
                arg0.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
            else {
                selectedContacts.remove(contact);
                arg0.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.listViewDefaultColor));
                if(selectedContacts.size() == 0)
                    clearSelection();
            }
        } else {
            Intent intent = new Intent(this, ContactActivity.class);
            intent.putExtra(EXTRA_MESSAGE, contact.getId());
            startActivityForResult(intent, RETURN_CODE);
        }
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(contact.getName());
        numberToCall = contact.getNumber();
        builder.setMessage(String.format("Do you really want to call %s (%s)?", contact.getName(), numberToCall));
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", null);
        builder.create().show();
        */
        //Toast.makeText(getApplicationContext(),  contact.getName() + " clicked", Toast.LENGTH_SHORT).show();
    }

    @OnItemLongClick(R.id.contactList)
    public boolean itemLongClicked(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Contact contact = (Contact) arg0.getItemAtPosition(position);
        if(!deleting) {
            deleting = true;
            floatingActionButton.setImageResource(R.drawable.ic_delete_black_24dp);
        }
        if(!selectedContacts.containsKey(contact)) {
            selectedContacts.put(contact, position);
            arg0.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        else {
            selectedContacts.remove(contact);
            arg0.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.listViewDefaultColor));
            if(selectedContacts.size() == 0)
                clearSelection();
        }
        return true;

    }


    @OnClick(R.id.floatingActionButton)
    public void fabClicked() {
        //Toast.makeText(getApplicationContext(), "Add clicked", Toast.LENGTH_SHORT).show();
        if(deleting) {
            realm.beginTransaction();
            for(Contact c: selectedContacts.keySet()) {
                contactAdapter.remove(c);
                c.deleteFromRealm();
            }
            realm.commitTransaction();
            clearSelection();
            return;
        }
        clearSelection();
        Intent intent = new Intent(this, ContactActivity.class);
        startActivityForResult(intent, RETURN_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (RETURN_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra("id", -1);
                    boolean updated = data.getBooleanExtra("updated", false);
                    if(id != -1) {
                        if(updated) {
                            contactAdapter.remove(realm.where(Contact.class).equalTo("id", id).findFirst());
                            contactAdapter.notifyDataSetChanged();
                        }
                        Contact contact = realm.where(Contact.class).equalTo("id", id).findFirst();
                        contactAdapter.add(contact);
                    }
                    contactAdapter.sort(new Comparator<Contact>() {
                        @Override
                        public int compare(Contact contact, Contact t1) {
                            return contact.getName().compareToIgnoreCase(t1.getName());
                        }
                    });
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(deleting)
            clearSelection();
    }

    private void clearSelection() {
        deleting = false;
        for(Integer c: selectedContacts.values()) {
            contactList.getChildAt(c).setBackgroundColor(getResources().getColor(R.color.listViewDefaultColor));
        }
        selectedContacts.clear();
        floatingActionButton.setImageResource(R.drawable.ic_add_black_24dp);
    }

}
