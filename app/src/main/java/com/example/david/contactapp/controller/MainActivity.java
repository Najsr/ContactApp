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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.david.contactapp.ContactAdapter;
import com.example.david.contactapp.PermissionsHelper;
import com.example.david.contactapp.R;
import com.example.david.contactapp.model.Contact;
import com.example.david.contactapp.model.Database;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.contactList)
    ListView contactList;

    private String numberToCall = "";

    private Database db;

    public static final String EXTRA_MESSAGE = "secretMessage";

    public static final int RETURN_CODE = 155;

    private List<Contact> contacts;

    private int lastContactId = -1;

    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        db = new Database(this);
        if(db.numberOfRows() == 0)
            contacts = new ArrayList<>();
        else
            contacts = db.getAllContacts();
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
        lastContactId = contacts.indexOf(contact);
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
        db.deleteContact(contact.getId());
        if(contacts.remove(contact))
            Toast.makeText(getApplicationContext(), contact.getName() + " long clicked (id: " + contact.getId() + ")", Toast.LENGTH_SHORT).show();
        contactAdapter.notifyDataSetChanged();
        contactAdapter.sort(new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact t1) {
                return contact.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
            }
        });
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
                            contacts.remove(lastContactId);
                            contactAdapter.notifyDataSetChanged();
                        }
                        Contact contact = db.getData(id);
                        contacts.add(contact);
                        Toast.makeText(getApplicationContext(), contact.getName() + " was added (id: " + contact.getId() + ")", Toast.LENGTH_SHORT).show();
                        contactAdapter.notifyDataSetChanged();
                    }
                    contactAdapter.sort(new Comparator<Contact>() {
                        @Override
                        public int compare(Contact contact, Contact t1) {
                            return contact.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
                        }
                    });
                }
                break;
            }
        }
    }

    private void reloadContacts() {
        contactAdapter.clear();
        contactAdapter.addAll(db.getAllContacts());
        contactAdapter.notifyDataSetChanged();
    }




}
