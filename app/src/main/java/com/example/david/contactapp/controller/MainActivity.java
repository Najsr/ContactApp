package com.example.david.contactapp.controller;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.contactList)
    ListView contactList;

    private String numberToCall = "";

    public static final String EXTRA_MESSAGE = "secretMessage";

    public static final int RETURN_CODE = 155;

    private List<Contact> contacts;

    private ContactAdapter contactAdapter;

    private ArrayList checkedItems = new ArrayList<>(); // for holding list item ids

    private Realm realm;

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
        PermissionsHelper.RequestPermission(this, Manifest.permission.CALL_PHONE);
        PermissionsHelper.RequestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        contactAdapter = new ContactAdapter(this, R.layout.contacts_layout, contacts);
        contactList.setAdapter(contactAdapter);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numberToCall));
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            getApplicationContext().startActivity(intent);
            Toast.makeText(getApplicationContext(), "calling" + numberToCall, Toast.LENGTH_SHORT).show();
        }
    };

    @OnItemClick(R.id.contactList)
    public void itemClicked(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Contact contact = (Contact) arg0.getItemAtPosition(position);
        Intent intent = new Intent(this, ContactActivity.class);
        intent.putExtra(EXTRA_MESSAGE, contact.getId());
        startActivityForResult(intent, RETURN_CODE);

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
        /* TO DO
        contactList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        contactList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                int checkedCount = contactList.getCheckedItemCount();

                actionMode.setTitle(checkedCount + " items selected");
                if(checked)
                    checkedItems.add(id);
                else
                    checkedItems.remove(id);

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.delete_menu_option, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                final int deleteSize = checkedItems.size();
                int itemId = menuItem.getItemId();
                if(itemId == R.id.delete){
                    SparseBooleanArray selected = contactAdapter.getSelectedIds();
                    // Captures all selected ids with a loop
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            Contact selectedListItem = contactAdapter.getItem(selected.keyAt(i));
                            // Remove selected items using ids
                            contactAdapter.remove(selectedListItem);
                        }
                    }
                    actionMode.finish();
                    return true;
                } else
                    return false;

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });
        /*contactAdapter.remove(contact);
        Toast.makeText(getApplicationContext(), contact.getName() + " long clicked (id: " + contact.getId() + ")", Toast.LENGTH_SHORT).show();
        contactAdapter.sort(new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact t1) {
                return contact.getName().compareToIgnoreCase(t1.getName());
            }
        });
        */
        return true;

    }

    @OnClick(R.id.floatingActionButton)
    public void fabClicked() {
        //Toast.makeText(getApplicationContext(), "Add clicked", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), contact.getName() + " was added (id: " + contact.getId() + ")", Toast.LENGTH_SHORT).show();
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

}
