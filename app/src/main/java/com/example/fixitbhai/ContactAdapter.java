package com.example.fixitbhai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements android.widget.Filterable {

    private List<Contact> contactList;
    private List<Contact> contactListFull;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
        this.contactListFull = new ArrayList<>(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());

        // Card Click Listener -> Detail View
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("EXTRA_CONTACT", contact);
            context.startActivity(intent);
        });

        // Call Action
        if (holder.btnCall != null) {
            holder.btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact.getPhone()));
                context.startActivity(intent);
            });
        }

        // Native Share Action
        if (holder.btnShare != null) {
            holder.btnShare.setOnClickListener(v -> {
                String shareMessage = "FixitBhai Technician Contact:\n" +
                        "Name: " + contact.getName() + "\n" +
                        "Service: " + contact.getCategory() + "\n" +
                        "Phone: " + contact.getPhone();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                context.startActivity(Intent.createChooser(shareIntent, "Share Technician Via"));
            });
        }
    }

    @Override
    public int getItemCount() {
        return contactList != null ? contactList.size() : 0;
    }

    public void updateData(List<Contact> newList) {
        this.contactList = newList;
        this.contactListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public android.widget.Filter getFilter() {
        return contactFilter;
    }

    private final android.widget.Filter contactFilter = new android.widget.Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(contactListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Contact item : contactListFull) {
                    if (item.getCategory() != null && item.getCategory().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    } else if (item.getName() != null && item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactList.clear();
            if (results.values != null) {
                contactList.addAll((List<Contact>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnCall, btnShare;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}