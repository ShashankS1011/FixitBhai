package com.example.fixitbhai;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private final List<Contact> contactList;
    private List<Contact> contactListFull;

    private String currentCategoryFilter = "";
    private String currentCityFilter = "";

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

        // FORMAT THE NUMBER FOR DISPLAY HERE
        holder.tvPhone.setText(formatForDisplay(contact.getPhone()));

        if (holder.tvCategoryCity != null) {
            String category = contact.getCategory() != null ? contact.getCategory() : "General";
            String city = contact.getCity() != null ? contact.getCity() : "";
            String categoryCityText = city.isEmpty() ? category : category + " • " + city;
            holder.tvCategoryCity.setText(categoryCityText);
        }

        if (holder.tvAvatar != null) {
            String name = contact.getName();
            String initial = (name != null && !name.trim().isEmpty()) ? name.trim().substring(0, 1).toUpperCase() : "F";
            holder.tvAvatar.setText(initial);
        }

        // --- NEW: Cloud vs Local UI Styling ---
        if (contact.isCloud()) {
            holder.tvSourceBadge.setText("☁️ Cloud");
            holder.tvSourceBadge.setBackgroundColor(Color.parseColor("#0F62FE")); // Modern Blue background
            holder.cardContact.setStrokeColor(Color.parseColor("#3B82F6")); // Blue Card border
        } else {
            holder.tvSourceBadge.setText("📱 Local");
            holder.tvSourceBadge.setBackgroundColor(Color.parseColor("#10B981")); // Emerald Green background
            holder.cardContact.setStrokeColor(Color.parseColor("#334155")); // Dark slate border
        }
        // ----------------------------------------

        if (contact.isDoNotCall()) {
            holder.tvName.setTextColor(Color.RED);
        } else {
            holder.tvName.setTextColor(Color.WHITE);
        }

        // Standard Click -> Open Details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("EXTRA_CONTACT", contact);
            context.startActivity(intent);
        });

        // Long Press -> Upload to Cloud
        holder.itemView.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Backup to Cloud ☁️")
                    .setMessage("Do you want to manually upload " + contact.getName() + " to your cloud backup?")
                    .setPositiveButton("Upload", (dialog, which) -> {
                        String uid = FirebaseAuth.getInstance().getUid();
                        if (uid != null) {
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .collection("contacts")
                                    // Use a clean number for the document ID
                                    .document(contact.getPhone().replaceAll("[^0-9+]", ""))
                                    .set(contact.toMap())
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, contact.getName() + " synced successfully!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });

        if (holder.btnCall != null) {
            holder.btnCall.setOnClickListener(v -> {
                if (contact.isDoNotCall()) {
                    Toast.makeText(context, "Warning: Contact marked as Do Not Call!", Toast.LENGTH_SHORT).show();
                }
                // Strip spaces so Android Dialer doesn't get confused
                String cleanPhoneForDialer = contact.getPhone().replaceAll("[^0-9+]", "");
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + cleanPhoneForDialer));
                context.startActivity(intent);
            });
        }

        if (holder.btnShare != null) {
            holder.btnShare.setOnClickListener(v -> {
                String shareMessage = "FixitBhai Technician Contact:\n" +
                        "Name: " + contact.getName() + "\n" +
                        "Service: " + contact.getCategory() + "\n" +
                        "City: " + contact.getCity() + "\n" +
                        "Phone: " + formatForDisplay(contact.getPhone()); // Formatted for share too

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                context.startActivity(Intent.createChooser(shareIntent, "Share Technician Via"));
            });
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateData(List<Contact> newList) {
        this.contactListFull = new ArrayList<>(newList);
        getFilter().filter(currentCategoryFilter);
    }

    public void filterBy(String category, String city) {
        this.currentCategoryFilter = (category != null) ? category.trim() : "";
        this.currentCityFilter = (city != null) ? city.trim().toLowerCase() : "";
        getFilter().filter(currentCategoryFilter);
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private final Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filteredList = new ArrayList<>();
            String categoryPattern = (constraint == null) ? "" : constraint.toString().toLowerCase().trim();

            for (Contact item : contactListFull) {
                boolean matchesCategory = categoryPattern.isEmpty()
                        || categoryPattern.equalsIgnoreCase("all")
                        || (item.getCategory() != null && item.getCategory().toLowerCase().contains(categoryPattern))
                        || (item.getName() != null && item.getName().toLowerCase().contains(categoryPattern));

                boolean matchesCity = currentCityFilter.isEmpty()
                        || (item.getCity() != null && item.getCity().equalsIgnoreCase(currentCityFilter));

                if (matchesCategory && matchesCity) {
                    filteredList.add(item);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
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

    // --- HELPER METHOD TO FORCE UI FORMATTING ---
    private String formatForDisplay(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) return "";

        // Strip everything but digits
        String digitsOnly = rawNumber.replaceAll("[^0-9]", "");

        // Extract the last 10 digits if it has the country code
        if (digitsOnly.length() >= 10) {
            String last10 = digitsOnly.substring(digitsOnly.length() - 10);
            return "+91 " + last10.substring(0, 5) + " " + last10.substring(5);
        }

        return rawNumber; // Fallback just in case
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvSourceBadge, tvAvatar, tvCategoryCity;
        ImageButton btnCall, btnShare;
        MaterialCardView cardContact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnShare = itemView.findViewById(R.id.btnShare);

            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvCategoryCity = itemView.findViewById(R.id.tvCategoryCity);

            // Initialize the new views here
            tvSourceBadge = itemView.findViewById(R.id.tvSourceBadge);
            cardContact = itemView.findViewById(R.id.cardContact);
        }
    }
}