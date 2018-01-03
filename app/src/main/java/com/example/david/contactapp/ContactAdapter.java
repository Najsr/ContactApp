package com.example.david.contactapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.david.contactapp.controller.MainActivity;
import com.example.david.contactapp.model.Contact;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private int layoutResource;

    private List<Contact> contactList;

    private Filter filter;

    private String numberToCall = "";

    private CustomFilter customFilter;

    public ContactAdapter(Context context, int layoutResource, List<Contact> contactList) {
        super(context, layoutResource, contactList);
        this.layoutResource = layoutResource;
        this.contactList = contactList;
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
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                PermissionsHelper.RequestPermission((MainActivity)getContext(), Manifest.permission.CALL_PHONE);
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

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = contactList;
                results.count = contactList.size();
            } else {
                // We perform filtering operation
                List<Contact> nContactList = new ArrayList<>();
                String constraintLowerCased = constraint.toString().toLowerCase();
                for (Contact p : contactList) {
                    if (p.getName().toLowerCase().startsWith(constraintLowerCased))
                        nContactList.add(p);
                }

                results.values = nContactList;
                results.count = nContactList.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.e("COUNT OF SHIT", Integer.toString(results.count));
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                contactList = (List<Contact>)results.values;
                notifyDataSetChanged();
            }
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (customFilter == null)
            customFilter = new CustomFilter();
        return customFilter;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Contact getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView avatarImageView;
        private ImageView callImageView;
    }
}