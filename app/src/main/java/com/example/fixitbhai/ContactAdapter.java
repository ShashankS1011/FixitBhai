package com.example.fixitbhai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements android.widget.Filterable {

    private List<Contact> contactList;
    private List<Contact> contactListFull; // Backup list for filtering

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
        holder.tvCategory.setText(contact.getCategory());

        // 1. Direct Call Action
        if (holder.btnCall != null) {
            holder.btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact.getPhone()));
                context.startActivity(intent);
            });
        }

        // 2. WhatsApp Action
        if (holder.btnWhatsapp != null) {
            holder.btnWhatsapp.setOnClickListener(v -> {
                String cleanPhone = contact.getPhone().replaceAll("[^0-9]", "");
                if (cleanPhone.length() == 10) {
                    cleanPhone = "91" + cleanPhone; // Country code (+91)
                }

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + cleanPhone + "&text="));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 3. Native Share Contact Action
        if (holder.btnShare != null) {
            holder.btnShare.setOnClickListener(v -> {
                String shareMessage = "Contact Details:\n" +
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

    // Call this custom method in MainActivity whenever contactList is modified
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
        TextView tvName, tvPhone, tvCategory;
        ImageButton btnCall, btnWhatsapp, btnShare;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnWhatsapp = itemView.findViewById(R.id.btnWhatsapp);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}