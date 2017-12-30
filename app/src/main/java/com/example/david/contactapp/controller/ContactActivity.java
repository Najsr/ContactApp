package com.example.david.contactapp.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.david.contactapp.PermissionsHelper;
import com.example.david.contactapp.R;
import com.example.david.contactapp.model.Contact;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.example.david.contactapp.controller.MainActivity.EXTRA_MESSAGE;

public class ContactActivity extends Activity {

    private static final int GALLERY = 999;
    private static final int CAMERA = 1000;

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
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.CAMERA);
        PermissionsHelper.RequestPermissions(this, permissions);
        showPictureDialog();
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(galleryIntent, GALLERY);
        } catch (Exception ex) {
            Toast.makeText(this, "No permission for reading external storage :(", Toast.LENGTH_SHORT).show();
        }
    }

    public void takePhotoFromCamera() {
        PermissionsHelper.RequestPermission(this, Manifest.permission.CAMERA);
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, CAMERA);
        } catch (Exception ex) {
            Toast.makeText(this, "No camera permission :(", Toast.LENGTH_SHORT).show();
        }
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                Picasso.with(this).load(contentURI).into(avatar);
                imageUri = contentURI.toString();
                imageSet = true;
            }

        } else if (requestCode == CAMERA) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri uri = getImageUri(getApplicationContext(), photo);
            Picasso.with(this).load(uri).into(avatar);
            imageUri = uri.toString();
            imageSet = true;
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}
