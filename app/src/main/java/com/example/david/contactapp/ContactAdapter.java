package com.example.david.contactapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.contactapp.R;
import com.example.david.contactapp.model.Contact;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private int layoutResource;

    public ContactAdapter(Context context, int layoutResource, List<Contact> contactList) {
        super(context, layoutResource, contactList);
        this.layoutResource = layoutResource;
        Picasso.with(getContext()).setLoggingEnabled(true);
    }



    @BindView(R.id.imageViewAvatar)
    ImageView image;

    @BindView(R.id.textViewName)
    TextView name;

    @BindView(R.id.textViewEmail)
    TextView email;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(layoutResource, null);
            ButterKnife.bind(this, view);
        }

        Contact contact = getItem(position);

        if (contact != null) {
            email.setText(contact.getEmail());
            name.setText(contact.getName());
            if(contact.getImagePath().isEmpty())
                Picasso.with(getContext()).load(R.mipmap.ic_launcher).into(image);
            else
                Picasso.with(getContext()).load(Uri.parse(contact.getImagePath())).into(image);
        }

        return view;
    }

}