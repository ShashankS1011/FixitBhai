package com.example.fixitbhai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudContactAdapter extends RecyclerView.Adapter<CloudContactAdapter.ViewHolder> implements Filterable {

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    private final List<Contact> originalList;
    private final List<Contact> filteredList;
    private final Set<String> selectedPhones = new HashSet<>();
    private final Set<String> existingLocalPhones;
    private final OnSelectionChangeListener listener;

    private String currentCategoryFilter = "";

    public CloudContactAdapter(List<Contact> cloudContacts, Set<String> existingLocalPhones, OnSelectionChangeListener listener) {
        this.originalList = new ArrayList<>(cloudContacts);
        this.filteredList = new ArrayList<>(cloudContacts);
        this.existingLocalPhones = existingLocalPhones != null ? existingLocalPhones : new HashSet<>();
        this.listener = listener;

        // Auto-select non-existing contacts initially
        for (Contact c : cloudContacts) {
            if (c.getPhone() != null && !this.existingLocalPhones.contains(c.getPhone())) {
                selectedPhones.add(c.getPhone());
            }
        }
        if (listener != null) listener.onSelectionChanged(selectedPhones.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cloud_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = filteredList.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvCategoryCity.setText((contact.getCategory() != null ? contact.getCategory() : "General") + " • " + contact.getCity());
        holder.tvPhone.setText(contact.getPhone());

        String initial = (contact.getName() != null && !contact.getName().trim().isEmpty()) ? contact.getName().trim().substring(0, 1).toUpperCase() : "F";
        holder.tvAvatar.setText(initial);

        String uploader = contact.getUploadedBy();
        if (uploader != null && !uploader.isEmpty()) {
            if (ContactAdapter.isAdminEmail(uploader)) {
                holder.tvUploader.setText("Verified by Admin");
                holder.tvUploader.setTextColor(android.graphics.Color.parseColor("#EF4444")); // Warning Red
                holder.tvUploader.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.tvUploader.setText("Verified by " + uploader);
                holder.tvUploader.setTextColor(android.graphics.Color.parseColor("#38BDF8")); // Cyan
                holder.tvUploader.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            holder.tvUploader.setVisibility(View.VISIBLE);
        } else {
            holder.tvUploader.setVisibility(View.GONE);
        }

        if (contact.isEmergency()) {
            holder.tvEmergencyBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvEmergencyBadge.setVisibility(View.GONE);
        }

        boolean isSelected = selectedPhones.contains(contact.getPhone());
        holder.cbSelect.setChecked(isSelected);

        if (isSelected) {
            holder.cardCloudContact.setStrokeColor(android.graphics.Color.parseColor("#38BDF8"));
            holder.cardCloudContact.setCardBackgroundColor(android.graphics.Color.parseColor("#1E293B"));
        } else {
            holder.cardCloudContact.setStrokeColor(android.graphics.Color.parseColor("#334155"));
            holder.cardCloudContact.setCardBackgroundColor(android.graphics.Color.parseColor("#0F172A"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPhones.contains(contact.getPhone())) {
                selectedPhones.remove(contact.getPhone());
            } else {
                selectedPhones.add(contact.getPhone());
            }
            notifyItemChanged(position);
            if (listener != null) listener.onSelectionChanged(selectedPhones.size());
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public List<Contact> getSelectedContacts() {
        List<Contact> selected = new ArrayList<>();
        for (Contact c : originalList) {
            if (c.getPhone() != null && selectedPhones.contains(c.getPhone())) {
                selected.add(c);
            }
        }
        return selected;
    }

    public void selectAll(boolean select) {
        selectedPhones.clear();
        if (select) {
            for (Contact c : filteredList) {
                if (c.getPhone() != null) selectedPhones.add(c.getPhone());
            }
        }
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedPhones.size());
    }

    public boolean isAllSelected() {
        return !filteredList.isEmpty() && selectedPhones.size() >= filteredList.size();
    }

    public void filterByCategory(String category) {
        this.currentCategoryFilter = category != null ? category.trim() : "";
        getFilter().filter(currentCategoryFilter);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Contact> resultList = new ArrayList<>();
                String query = (constraint == null) ? "" : constraint.toString().toLowerCase().trim();

                for (Contact item : originalList) {
                    boolean matchesQuery = query.isEmpty()
                            || (item.getName() != null && item.getName().toLowerCase().contains(query))
                            || (item.getCategory() != null && item.getCategory().toLowerCase().contains(query))
                            || (item.getPhone() != null && item.getPhone().contains(query))
                            || (item.getUploadedBy() != null && item.getUploadedBy().toLowerCase().contains(query));

                    boolean matchesCategory = currentCategoryFilter.isEmpty()
                            || currentCategoryFilter.equalsIgnoreCase("all")
                            || (item.getCategory() != null && item.getCategory().equalsIgnoreCase(currentCategoryFilter));

                    if (matchesQuery && matchesCategory) {
                        resultList.add(item);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultList;
                results.count = resultList.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<Contact>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCloudContact;
        CheckBox cbSelect;
        TextView tvAvatar, tvName, tvCategoryCity, tvPhone, tvUploader, tvEmergencyBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCloudContact = itemView.findViewById(R.id.cardCloudContact);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategoryCity = itemView.findViewById(R.id.tvCategoryCity);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvUploader = itemView.findViewById(R.id.tvUploader);
            tvEmergencyBadge = itemView.findViewById(R.id.tvEmergencyBadge);
        }
    }
}