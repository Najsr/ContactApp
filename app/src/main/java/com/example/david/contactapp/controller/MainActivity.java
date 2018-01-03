package com.example.david.contactapp.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

import com.example.david.contactapp.ContactAdapter;
import com.example.david.contactapp.R;
import com.example.david.contactapp.model.Contact;
import com.melnykov.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

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
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.contactList)
    ListView contactList;

    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;

    @BindView(R.id.imageViewRemove)
    ImageView imageViewRemove;

    @BindView(R.id.editTextSearch)
    MaterialEditText editTextSearch;

    @BindView(R.id.toolbar)
    Toolbar toolBar;

    public static final String EXTRA_MESSAGE = "secretMessage";

    public static final int RETURN_CODE = 155;

    private List<Contact> contacts;

    private ContactAdapter contactAdapter;

    private Realm realm;

    private boolean deleting = false;

    private boolean wasAZ = true;

    private Map<Contact, Integer> selectedContacts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolBar);
        setContentView(R.layout.activity_main);
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

    @OnClick(R.id.imageViewRemove)
    public void onRemoveClick(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
        clearSelection();
    }

    @OnClick(R.id.imageViewSort)
    public void onSortClick(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));
        if(!deleting) {
            sort(!wasAZ);
            wasAZ = !wasAZ;
        }
    }

    @OnTextChanged(R.id.editTextSearch)
    public void editTextSearchTextChanged(CharSequence s) {
        contactAdapter.getFilter().filter(s.toString());
    }

    @OnTouch
    public boolean onContactListClick(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN &&
                contactList.pointToPosition((int) (event.getX() * event.getXPrecision()), (int) (event.getY() * event.getYPrecision())) == -1) {
            editTextSearch.clearFocus();
            hideKeyboard(this);
            return true;
        }
        return false;
    }

    @OnItemClick(R.id.contactList)
    public void itemClicked(AdapterView<?> arg0, View arg1, int position, long arg3) {
        editTextSearch.clearFocus();
        hideKeyboard(this);
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
    }

    @OnItemLongClick(R.id.contactList)
    public boolean itemLongClicked(AdapterView<?> arg0, View arg1, int position, long arg3) {
        editTextSearch.clearFocus();
        hideKeyboard(this);
        Contact contact = (Contact) arg0.getItemAtPosition(position);
        if(!deleting) {
            startDeletion();
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
        editTextSearch.clearFocus();
        hideKeyboard(this);
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

                        sort(true);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        editTextSearch.clearFocus();
        hideKeyboard(this);
        if(deleting)
            clearSelection();
    }

    private void startDeletion() {
        deleting = true;
        floatingActionButton.setImageResource(R.drawable.ic_delete_black_24dp);
        imageViewRemove.setVisibility(View.VISIBLE);
        //toolBar.setBackgroundColor(getResources().getColor(R.color.colorAccentDark));
    }
    private void clearSelection() {
        deleting = false;
        imageViewRemove.setVisibility(View.INVISIBLE);
        //toolBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        int color = getResources().getColor(R.color.listViewDefaultColor);
        for(Integer c: selectedContacts.values()) {
            contactList.getChildAt(c).setBackgroundColor(color);
        }
        selectedContacts.clear();
        floatingActionButton.setImageResource(R.drawable.ic_add_black_24dp);
    }

    private void sort(boolean AZ) {
        if(AZ) {
            contactAdapter.sort(new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact t1) {
                    return contact.getName().compareToIgnoreCase(t1.getName());
                }
            });
        } else {
            contactAdapter.sort(new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact t1) {
                    return t1.getName().compareToIgnoreCase(contact.getName());
                }
            });
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
