package com.example.fixitbhai.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fixitbhai.Contact;
import com.example.fixitbhai.ContactAdapter;
import com.example.fixitbhai.databinding.FragmentHomeBinding;
import com.example.fixitbhai.util.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment renders technician contact list and reacts to UI state changes.
 * Contains no business logic; only observes ViewModel state and forwards user events.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ContactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupSearchInput();
        setupObservers();

        binding.btnRetry.setOnClickListener(v -> viewModel.loadContacts());
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(new ArrayList<>());
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewContacts.setAdapter(adapter);
    }

    private void setupSearchInput() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filterBy(s != null ? s.toString() : "", "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getContactsLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    showLoadingState();
                    break;
                case SUCCESS:
                    showSuccessState(resource.getData());
                    break;
                case EMPTY:
                    showEmptyState();
                    break;
                case ERROR:
                    showErrorState(resource.getMessage());
                    break;
            }
        });

        viewModel.getToastEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String message = event.getContentIfNotHandled();
                if (message != null) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoadingState() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewContacts.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.errorStateLayout.setVisibility(View.GONE);
    }

    private void showSuccessState(List<Contact> data) {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewContacts.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.errorStateLayout.setVisibility(View.GONE);

        if (data != null && adapter != null) {
            adapter.updateData(data);
        }
    }

    private void showEmptyState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewContacts.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
        binding.errorStateLayout.setVisibility(View.GONE);
    }

    private void showErrorState(String errorMessage) {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewContacts.setVisibility(View.GONE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.errorStateLayout.setVisibility(View.VISIBLE);

        binding.tvErrorMessage.setText(errorMessage != null ? errorMessage : "An unexpected error occurred");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify binding to avoid memory leak when fragment view is destroyed
        binding = null;
    }
}