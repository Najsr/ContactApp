package com.example.david.contactapp.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.david.contactapp.PermissionsHelper;
import com.example.david.contactapp.R;
import com.example.david.contactapp.model.Contact;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.example.david.contactapp.controller.MainActivity.EXTRA_MESSAGE;

public class ContactActivity extends Activity {

    private static final int PICK_IMAGE = 25;

    private int id = -1;

    private boolean imageSet = false;

    private Realm realm;

    @BindView(R.id.editTextName)
    MaterialEditText editTextName;

    @BindView(R.id.editTextEmail)
    MaterialEditText editTextEmail;

    @BindView(R.id.editTextPhone)
    MaterialEditText editTextPhone;

    @BindView(R.id.imageViewAvatar)
    ImageView avatar;

    String imageUri = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        realm = Realm.getDefaultInstance();
        id = intent.getIntExtra(EXTRA_MESSAGE, -1);
        if(id > 0)
            loadValues(realm.where(Contact.class).equalTo("id", id).findFirst());
    }

    @OnClick(R.id.imageViewAvatar)
    public void imageClicked() {
        PermissionsHelper.RequestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @OnClick(R.id.buttonSave)
    public void addClicked() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        if(name.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || !Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(getApplicationContext(), "Please check your input data!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = getIntent();
        Contact contact = realm.where(Contact.class).equalTo("id", id).findFirst();
        boolean exists = contact != null;
        if(exists) {
            realm.beginTransaction();
            contact.setName(name);
            contact.setEmail(email);
            contact.setNumber(phone);
            if(imageSet)
                contact.setImagePath(imageUri);
            else if (contact.getImagePath() == null)
                contact.setImagePath("");

            realm.commitTransaction();
            //database.updateContact(id, name, phone, imageUri, email);
            intent.putExtra("updated", true);
        } else {
            realm.beginTransaction();

            Integer lastId;
            try{
               lastId = realm.where(Contact.class).max("id").intValue();
            } catch (NullPointerException ex) {
                lastId = 0;
            }
            int nextId = lastId + 1;
            id = nextId;
            contact = realm.createObject(Contact.class, nextId);
            contact.setName(name);
            contact.setEmail(email);
            contact.setNumber(phone);
            if(imageSet)
                contact.setImagePath(imageUri);
            else if (contact.getImagePath() == null)
                contact.setImagePath("");

            realm.commitTransaction();
            //id = (int)database.insertContact(name, phone, imageUri, email);

        }
        intent.putExtra("id", id);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void loadValues(Contact contact) {
        if(contact == null)
            return;
        editTextName.setText(contact.getName());
        editTextEmail.setText(contact.getEmail());
        editTextPhone.setText(contact.getNumber());
        String imagePath = contact.getImagePath();
        if(!imagePath.isEmpty()) {
            Picasso.with(this).load(Uri.parse(contact.getImagePath())).into(avatar);
            imageUri = imagePath;
        }
        editTextName.setSelection(editTextName.getText().length());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Picasso.with(this).load(data.getData()).into(avatar);
            imageUri = data.getData().toString();
            imageSet = true;
        }
    }
}
