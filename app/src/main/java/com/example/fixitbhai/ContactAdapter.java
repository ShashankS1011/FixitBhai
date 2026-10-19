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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private final List<Contact> contactListFiltered;
    private List<Contact> contactListFull;

    private String currentCategoryFilter = "";
    private String currentCityFilter = "";

    public ContactAdapter(List<Contact> contactList) {
        this.contactListFull = new ArrayList<>(contactList);
        this.contactListFiltered = new ArrayList<>(contactList);
    }

    public static boolean isAdminEmail(String email) {
        if (email == null) return false;
        String lower = email.toLowerCase().trim();
        return lower.contains("shashanksingh631@gmail.com") || lower.contains("shashankkawai631@gmail.com") || lower.equals("admin");
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        if (position < 0 || position >= contactListFiltered.size()) return;
        Contact contact = contactListFiltered.get(position);
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

        if (holder.tvUploader != null) {
            String uploader = contact.getUploadedBy();
            if (contact.isCloud() || (uploader != null && !uploader.isEmpty())) {
                String uploaderText = (uploader != null && !uploader.isEmpty()) ? "Verified by " + uploader : "Verified Cloud Expert";
                holder.tvUploader.setText(uploaderText);
                holder.tvUploader.setVisibility(View.VISIBLE);
            } else {
                holder.tvUploader.setVisibility(View.GONE);
            }
        }

        if (holder.tvEmergencyBadge != null) {
            if (contact.isEmergency()) {
                holder.tvEmergencyBadge.setText("24/7");
                holder.tvEmergencyBadge.setBackgroundColor(Color.parseColor("#DC2626")); // High contrast Red
                holder.tvEmergencyBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvEmergencyBadge.setVisibility(View.GONE);
            }
        }

        // --- NEW: Cloud vs Local UI Styling ---
        if (contact.isCloud()) {
            holder.tvSourceBadge.setText("Cloud");
            holder.tvSourceBadge.setBackgroundColor(Color.parseColor("#0F62FE")); // Modern Blue background
            holder.cardContact.setStrokeColor(Color.parseColor("#3B82F6")); // Blue Card border
        } else {
            holder.tvSourceBadge.setText("Local");
            holder.tvSourceBadge.setBackgroundColor(Color.parseColor("#10B981")); // Emerald Green background
            holder.cardContact.setStrokeColor(Color.parseColor("#334155")); // Dark slate border
        }
        // ----------------------------------------

        if (contact.isDoNotCall()) {
            holder.tvName.setTextColor(Color.parseColor("#EF4444")); // Warning Red
        } else {
            holder.tvName.setTextColor(Color.parseColor("#F8FAFC")); // Slate White
        }

        // Standard Click -> Open Details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra("EXTRA_CONTACT", contact);
            context.startActivity(intent);
        });

        String uploader = contact.getUploadedBy();
        if (holder.tvUploader != null) {
            if (contact.isCloud() && uploader != null && !uploader.isEmpty()) {
                if (isAdminEmail(uploader)) {
                    holder.tvUploader.setText("Verified by Admin");
                    holder.tvUploader.setTextColor(Color.parseColor("#EF4444")); // Warning Red
                    holder.tvUploader.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    holder.tvUploader.setText("Verified by " + uploader);
                    holder.tvUploader.setTextColor(Color.parseColor("#38BDF8")); // Cyan
                    holder.tvUploader.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                holder.tvUploader.setVisibility(View.VISIBLE);
            } else {
                holder.tvUploader.setVisibility(View.GONE);
            }
        }

        // Long Press -> Upload to Shared Cloud
        holder.itemView.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Publish to Shared Cloud")
                    .setMessage("Upload " + contact.getName() + " to the public FixitBhai cloud directory so all users in " + contact.getCity() + " can access it?")
                    .setPositiveButton("Publish", (dialog, which) -> {
                        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String userEmail = user.getEmail() != null ? user.getEmail().trim() : "";
                            String displayName = user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty() ? user.getDisplayName().trim() : "";
                            String uploaderText;
                            if (isAdminEmail(userEmail)) {
                                uploaderText = "Admin";
                            } else if (!displayName.isEmpty() && !userEmail.isEmpty()) {
                                uploaderText = displayName + " (" + userEmail + ")";
                            } else if (!userEmail.isEmpty()) {
                                uploaderText = userEmail;
                            } else {
                                uploaderText = !displayName.isEmpty() ? displayName : "Verified Member";
                            }

                            // Keep local contact unaltered so local card stays strictly Local without cloud uploader tag
                            String cleanPhone = contact.getPhone().replaceAll("[^0-9+]", "");
                            String city = (contact.getCity() != null && !contact.getCity().trim().isEmpty()) ? contact.getCity().trim() : "Prayagraj";

                            // Create a copy payload with isCloud = true for cloud directory
                            Contact cloudPayload = new Contact(contact.getName(), contact.getPhone(), contact.getCategory(), city, true);
                            cloudPayload.setRating(contact.getRating());
                            cloudPayload.setNotes(contact.getNotes());
                            cloudPayload.setDoNotCall(contact.isDoNotCall());
                            cloudPayload.setEmergency(contact.isEmergency());
                            cloudPayload.setReviewTags(contact.getReviewTags());
                            cloudPayload.setUploadedBy(uploaderText);
                            cloudPayload.setUploaderUid(user.getUid());
                            cloudPayload.setVerified(true);

                            // 1. Publish to Shared City Cloud Directory for All Users
                            FirebaseFirestore.getInstance()
                                    .collection("cities")
                                    .document(city)
                                    .collection("contacts")
                                    .document(cleanPhone)
                                    .set(cloudPayload.toMap())
                                    .addOnSuccessListener(aVoid -> {
                                        // 2. Also save to personal backup collection
                                        FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.getUid())
                                                .collection("contacts")
                                                .document(cleanPhone)
                                                .set(cloudPayload.toMap());

                                        Toast.makeText(context, contact.getName() + " published to " + city + " cloud directory!", Toast.LENGTH_SHORT).show();
                                        notifyItemChanged(position);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Cloud upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        if (holder.btnWhatsApp != null) {
            holder.btnWhatsApp.setOnClickListener(v -> {
                String cleanPhone = contact.getPhone().replaceAll("[^0-9]", "");
                String message = "Hello " + contact.getName() + ", I found your contact on FixitBhai for " + contact.getCategory() + " service.";
                try {
                    Intent waIntent = new Intent(Intent.ACTION_VIEW);
                    waIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + cleanPhone + "&text=" + Uri.encode(message)));
                    context.startActivity(waIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show();
                }
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
        return contactListFiltered.size();
    }

    public Contact getContactAt(int position) {
        if (position >= 0 && position < contactListFiltered.size()) {
            return contactListFiltered.get(position);
        }
        return null;
    }

    public void removeContact(int position) {
        if (position >= 0 && position < contactListFiltered.size()) {
            Contact removed = contactListFiltered.remove(position);
            contactListFull.remove(removed);
            notifyItemRemoved(position);
        }
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
                        || (categoryPattern.contains("emergency") || categoryPattern.contains("24/7") ? item.isEmergency() : false)
                        || (item.getCategory() != null && item.getCategory().toLowerCase().contains(categoryPattern))
                        || (item.getName() != null && item.getName().toLowerCase().contains(categoryPattern))
                        || (item.getPhone() != null && item.getPhone().replaceAll("[^0-9]", "").contains(categoryPattern))
                        || (item.getNotes() != null && item.getNotes().toLowerCase().contains(categoryPattern))
                        || (item.getReviewTags() != null && item.getReviewTags().toLowerCase().contains(categoryPattern));

                boolean matchesCity = currentCityFilter.isEmpty()
                        || currentCityFilter.equalsIgnoreCase("all")
                        || currentCityFilter.equalsIgnoreCase("all cities")
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
            List<Contact> newList = (results.values != null) ? (List<Contact>) results.values : new ArrayList<>();
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return contactListFiltered.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Contact oldItem = contactListFiltered.get(oldItemPosition);
                    Contact newItem = newList.get(newItemPosition);
                    return oldItem.getPhone() != null && oldItem.getPhone().equals(newItem.getPhone());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Contact oldItem = contactListFiltered.get(oldItemPosition);
                    Contact newItem = newList.get(newItemPosition);
                    return String.valueOf(oldItem.getName()).equals(String.valueOf(newItem.getName()))
                            && String.valueOf(oldItem.getCity()).equals(String.valueOf(newItem.getCity()))
                            && String.valueOf(oldItem.getCategory()).equals(String.valueOf(newItem.getCategory()))
                            && oldItem.isCloud() == newItem.isCloud()
                            && oldItem.isEmergency() == newItem.isEmergency()
                            && oldItem.isDoNotCall() == newItem.isDoNotCall();
                }
            });

            contactListFiltered.clear();
            contactListFiltered.addAll(newList);
            diffResult.dispatchUpdatesTo(ContactAdapter.this);
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
        TextView tvName, tvPhone, tvSourceBadge, tvAvatar, tvCategoryCity, tvUploader, tvEmergencyBadge;
        ImageButton btnCall, btnShare, btnWhatsApp;
        MaterialCardView cardContact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnWhatsApp = itemView.findViewById(R.id.btnWhatsApp);

            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvCategoryCity = itemView.findViewById(R.id.tvCategoryCity);
            tvUploader = itemView.findViewById(R.id.tvUploader);
            tvEmergencyBadge = itemView.findViewById(R.id.tvEmergencyBadge);

            // Initialize the new views here
            tvSourceBadge = itemView.findViewById(R.id.tvSourceBadge);
            cardContact = itemView.findViewById(R.id.cardContact);
        }
    }
}