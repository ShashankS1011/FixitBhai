package com.example.fixitbhai;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fixitbhai.util.Resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository acting as the single source of truth for technician contacts data.
 * Coordinates local SQLite storage and remote synchronization.
 */
public class HomeRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executorService;

    public HomeRepository(@NonNull Context context) {
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Loads contact list asynchronously from the database and posts UI state.
     */
    public LiveData<Resource<List<Contact>>> getContacts() {
        MutableLiveData<Resource<List<Contact>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executorService.execute(() -> {
            try {
                List<Contact> contacts = dbHelper.getAllContacts();
                if (contacts.isEmpty()) {
                    result.postValue(Resource.empty());
                } else {
                    result.postValue(Resource.success(contacts));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Failed to load contacts: " + e.getLocalizedMessage(), null));
            }
        });

        return result;
    }
}