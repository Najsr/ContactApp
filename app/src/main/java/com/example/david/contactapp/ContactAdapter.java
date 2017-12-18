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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        Contact contact = getItem(position);

        if (contact != null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource,null);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.textViewName);
            holder.emailTextView = convertView.findViewById(R.id.textViewEmail);
            holder.avatarImageView = convertView.findViewById(R.id.imageViewAvatar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.emailTextView.setText(contact.getEmail());
        holder.nameTextView.setText(contact.getName());
        if(contact.getImagePath() == null || contact.getImagePath().isEmpty())
            Picasso.with(getContext()).load(R.mipmap.ic_launcher).into(holder.avatarImageView);
        else
            Picasso.with(getContext()).load(Uri.parse(contact.getImagePath())).into(holder.avatarImageView);
        return convertView;
    }

    static class ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView avatarImageView;
    }
}