package com.example.david.contactapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.contactapp.R;
import com.example.david.contactapp.controller.MainActivity;
import com.example.david.contactapp.model.Contact;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private int layoutResource;

    private String numberToCall = "";

    public ContactAdapter(Context context, int layoutResource, List<Contact> contactList) {
        super(context, layoutResource, contactList);
        this.layoutResource = layoutResource;
        Picasso.with(getContext()).setLoggingEnabled(true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final Contact contact = getItem(position);

        if (contact != null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource,null);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.textViewName);
            holder.emailTextView = convertView.findViewById(R.id.textViewEmail);
            holder.avatarImageView = convertView.findViewById(R.id.imageViewAvatar);
            holder.callImageView = convertView.findViewById(R.id.imageViewCall);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.callImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(contact.getName());
                numberToCall = contact.getNumber();
                builder.setMessage(String.format("Do you really want to call %s (%s)?", contact.getName(), numberToCall));
                builder.setPositiveButton("Yes", dialogClickListener);
                builder.setNegativeButton("No", null);
                builder.create().show();
            }
        });
        holder.emailTextView.setText(contact.getEmail());
        holder.nameTextView.setText(contact.getName());
        if(contact.getImagePath() == null || contact.getImagePath().isEmpty())
            Picasso.with(getContext()).load(R.mipmap.ic_launcher).into(holder.avatarImageView);
        else
            Picasso.with(getContext()).load(Uri.parse(contact.getImagePath())).into(holder.avatarImageView);
        return convertView;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numberToCall));
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getContext().startActivity(intent);
        }
    };

    static class ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView avatarImageView;
        private ImageView callImageView;
    }
}